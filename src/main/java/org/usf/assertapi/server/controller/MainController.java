package org.usf.assertapi.server.controller;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.of;
import static org.usf.assertapi.core.AssertionContext.buildContext;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.usf.assertapi.core.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.usf.assertapi.server.dao.RequestDao;
import org.usf.assertapi.server.dao.TraceDao;
import org.usf.assertapi.server.model.ApiRequestGroupServer;
import org.usf.assertapi.server.model.ApiRequestServer;
import org.usf.assertapi.server.DefaultResponseComparator;
import org.usf.assertapi.server.model.ApiResponseServer;
import org.usf.assertapi.server.service.RequestService;
import org.usf.assertapi.server.service.TraceService;


@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api")
public class MainController {

	private final TraceService traceService;
	private final RequestService requestService;

	private Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

	@GetMapping("/progress")
	public SseEmitter eventEmitter() throws IOException {
		SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
		UUID guid = UUID.randomUUID();
		sseEmitters.put(guid.toString(), sseEmitter);
		sseEmitter.send(SseEmitter.event().name("GUI_ID").data(guid));
		sseEmitter.onCompletion(() -> sseEmitters.remove(guid.toString()));
		sseEmitter.onTimeout(() -> sseEmitters.remove(guid.toString()));
		return sseEmitter;
	}

	@PostMapping("run")
	public long run(
			@RequestHeader(value = "GUI_ID") String guid,
			@RequestParam(name="actual_env") String actualEnv,
			@RequestParam(name="expected_env") String expectedEnv,
			@RequestParam(name="app") String app,
			@RequestParam(name="id", required = false) int[] ids,
			@RequestParam(name="disabled_id", required = false) int[] disabledIds,
			@RequestBody Configuration config) throws IOException {

		List<String> envs = asList(actualEnv, expectedEnv);
		var ctx = traceService.register(buildContext(), app, actualEnv, expectedEnv);
		var list = requestService.getRequestList(ids, envs, app).stream()
				.filter(r -> r.getRequestGroupList().stream().map(ApiRequestGroupServer::getEnv).collect(toList()).containsAll(envs))
				.map(ApiRequestServer::getRequest)
				.collect(toList());
		sseEmitters.get(guid).send(SseEmitter.event().name("nb tests launch " + guid).data(list.size()));
		if(disabledIds != null) {
			of(disabledIds).forEach(i-> list.stream().filter(t-> t.getId() == i).findAny().ifPresent(t-> t.getConfiguration().setEnable(false)));
		}
		var assertions = new ApiAssertionsFactory()
				.comparing(config.getRefer(), config.getTarget())
				.using(new DefaultResponseComparator())
				.trace(a-> {
					traceService.addTrace(ctx, a);
					try {
						sseEmitters.get(guid).send(SseEmitter.event().name(a.getId() + " end " + guid).data(a));
					} catch (IOException e) {
						log.error("server sent events fail", e);
					}
				})
				.build();
		list.forEach(q-> {
			try {
				sseEmitters.get(guid).send(SseEmitter.event().name("start " + guid).data(q));
				assertions.assertApi(q);
				log.info("TEST {} OK", q);
			}
			catch(Throwable e) {
				//fail
				log.error("assertion fail", e);
			}
		});
		sseEmitters.get(guid).complete();
		return ctx;
	}

	@PostMapping("run/{id}")
	public ResponseComparator run(
			@PathVariable("id") int id,
			@RequestBody Configuration config
	) {
		var responseComparator = new ResponseComparator();
		var expectedResponse = new ApiResponseServer();
		var actualResponse = new ApiResponseServer();
		var request = requestService.getRequestOne(id);
		var assertions = new DefaultApiAssertions(
				RestTemplateBuilder.build(requireNonNull(config.refer)),
				RestTemplateBuilder.build(requireNonNull(config.target)),
				r-> new ResponseProxyComparator(new DefaultResponseComparator(), t -> {responseComparator.setStatus(t.getStatus()); responseComparator.setStep(t.getStep());}, config.refer, config.target, r){
					@Override
					public void assertJsonContent(String expectedContent, String actualContent, boolean strict) {
						expectedResponse.setResponse(expectedContent);
						actualResponse.setResponse(actualContent);
						super.assertJsonContent(expectedContent, actualContent, strict);
					}

					@Override
					public void assertContentType(MediaType expectedContentType, MediaType actualContentType) {
						expectedResponse.setContentType(expectedContentType.toString());
						actualResponse.setContentType(actualContentType.toString());
						super.assertContentType(expectedContentType, actualContentType);
					}

					@Override
					public void assertStatusCode(int expectedStatusCode, int actualStatusCode) {
						expectedResponse.setStatusCode(expectedStatusCode);
						actualResponse.setStatusCode(actualStatusCode);
						super.assertStatusCode(expectedStatusCode, actualStatusCode);
					}
				});

		try {
			assertions.assertApi(request.getRequest());
			log.info("TEST {} OK", request);
		}
		catch(Throwable e) {
			//fail
			log.error("assertion fail", e);
		}

		return new ResponseComparator(expectedResponse, actualResponse);
	}

	@Getter
	@RequiredArgsConstructor
	public static final class Configuration {
		
		private final ServerConfig refer;
		private final ServerConfig target;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static final class ResponseComparator {
		private ApiResponseServer exp;
		private ApiResponseServer act;
		private TestStatus status;
		private TestStep step;

		ResponseComparator(ApiResponseServer exp, ApiResponseServer act) {
			this.exp = exp;
			this.act = act;
		}
	}
}
