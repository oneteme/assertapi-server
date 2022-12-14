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
import org.usf.assertapi.server.model.*;
import org.usf.assertapi.server.DefaultResponseComparator;
import org.usf.assertapi.server.service.RequestService;
import org.usf.assertapi.server.service.SseService;
import org.usf.assertapi.server.service.TraceService;


@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api")
public class MainController {

	private final TraceController traceController;
	private final TraceService traceService;
	private final RequestService requestService;
	private final SseService sseService;

	@GetMapping("/subscribe")
	public SseEmitter eventEmitter(
			@RequestParam(name="ctx") long ctx
	) {
		return sseService.subscribe(ctx);
	}

	@PostMapping("run")
	public void run(
			@RequestHeader(value = "ctx") long ctx,
			@RequestParam(name="app") String app,
			@RequestParam(name="actual_env") String actualEnv,
			@RequestParam(name="expected_env") String expectedEnv,
			@RequestParam(name="id", required = false) int[] ids,
			@RequestParam(name="disabled_id", required = false) int[] disabledIds,
			@RequestBody Configuration config) {

		List<String> envs = asList(actualEnv, expectedEnv);
		var list = requestService.getRequestList(ids, envs, app).stream()
				.filter(r -> r.getRequestGroupList().stream().map(ApiRequestGroupServer::getEnv).collect(toList()).containsAll(envs))
				.map(ApiRequestServer::getRequest)
				.collect(toList());
		if(disabledIds != null) {
			of(disabledIds).forEach(i-> list.stream().filter(t-> t.getId() == i).findAny().ifPresent(t-> t.getConfiguration().setEnable(false)));
		}
		sseService.start(ctx, new ApiTraceGroup(ctx, buildContext().getUser(), buildContext().getOs(), buildContext().getAddress(), app, actualEnv, expectedEnv, TraceGroupStatus.PENDING, list.size(), (int) list.stream().filter(l -> !l.getConfiguration().isEnable()).count()));

		var assertions = new ApiAssertionsFactory()
				.comparing(config.getRefer(), config.getTarget())
				.using(new DefaultResponseComparator())
				.trace(a-> {
					traceService.addTrace(ctx, a);
					sseService.update(ctx, a);
				})
				.build();
		list.forEach(q-> {
			try {
				assertions.assertApi(q);
				log.info("TEST {} OK", q);
			}
			catch(Throwable e) {
				//fail
				log.error("assertion fail", e);
			}
		});
		sseService.complete(ctx);
		// update register pour la fin du test
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
