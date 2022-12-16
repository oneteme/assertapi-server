package org.usf.assertapi.server.controller;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.of;
import static org.usf.assertapi.core.AssertionContext.CTX;
import static org.usf.assertapi.core.AssertionContext.buildContext;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.usf.assertapi.server.service.SseServiceImpl;
import org.usf.assertapi.server.service.TraceService;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/v1/assert/api")
public class MainController {

	private final RequestService requestService;
	private final TraceController traceController;
	private final TraceService traceService;
	private final RequestController requestController;
	private final SseService sseService;

	public MainController(RequestService requestService, TraceController traceController, TraceService traceService, RequestController requestController) {
		this.requestService = requestService;
		this.traceController = traceController;
		this.traceService = traceService;
		this.requestController = requestController;
		this.sseService = new SseServiceImpl(traceService::updateStatus);
	}

	//TODO change it to PathParam (ctx=>id)
	@GetMapping("/subscribe")
	public SseEmitter eventEmitter(@RequestParam(name="ctx") long ctx) {
		return sseService.subscribe(ctx);
	}

	@PostMapping("load")
	public ResponseEntity<List<ApiRequest>> load(
			@RequestHeader(value = CTX) String context,
			@RequestParam(name="app") String app,
			@RequestParam(name="stable_release") String stableRelease) {
		var ctx = AssertionContext.parseHeader(new ObjectMapper(), context);
		var id = traceService.register(ctx, app, ctx.getAddress(), stableRelease, TraceGroupStatus.PENDING);
		sseService.init(id);
		var list = requestController.get(null, app, List.of(stableRelease));
		sseService.start(id, new ApiTraceGroup(id, ctx.getUser(), ctx.getOs(), ctx.getAddress(), app,  ctx.getAddress(), stableRelease, TraceGroupStatus.PENDING, list.size(), (int) list.stream().filter(l -> !l.getConfiguration().isEnable()).count()));

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("trace", "/v1/assert/api/trace/" + id);

		return ResponseEntity.ok().headers(responseHeaders).body(list);
	}

	@PostMapping("run")
	public long run(
			@RequestParam(name="app") String app,
			@RequestParam(name="latest_release") String latestRelease,
			@RequestParam(name="stable_release") String stableRelease,
			@RequestParam(name="id", required = false) int[] ids,
			@RequestParam(name="excluded", required = false) boolean excluded,
			@RequestBody Configuration config) {
		var id = traceService.register(buildContext(), app, latestRelease, stableRelease, TraceGroupStatus.PENDING);
		sseService.init(id);
		List<String> envs = asList(latestRelease, stableRelease);
		var list = requestController.getAll(!excluded ? ids : null, app, envs).stream()
				.filter(r -> r.getRequestGroupList().stream().map(ApiRequestGroupServer::getEnv).collect(toList()).containsAll(envs))
				.map(ApiRequestServer::getRequest)
				.collect(toList());
		if(excluded && ids != null) {
			of(ids).forEach(i-> list.stream().filter(t-> t.getId() == i).findAny().ifPresent(t-> t.getConfiguration().setEnable(false)));
		}
		sseService.start(id, new ApiTraceGroup(id, buildContext().getUser(), buildContext().getOs(), buildContext().getAddress(), app, latestRelease, stableRelease, TraceGroupStatus.PENDING, list.size(), (int) list.stream().filter(l -> !l.getConfiguration().isEnable()).count()));

		var assertions = new ApiAssertionFactory()
				.comparing(config.getRefer(), config.getTarget())
				.using(new DefaultResponseComparator())
				.trace(a-> {
					traceController.put(id, a);
					sseService.update(id, a);
				})
				.build();
		assertions.assertApiAsync(list, () -> sseService.complete(id));
		return id;
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
		var assertions = new DefaultApiAssertion(
				new ResponseProxyComparator(new DefaultResponseComparator(), t -> {responseComparator.setStatus(t.getStatus()); responseComparator.setStep(t.getStep());}, new RequestExecution(config.refer.buildRootUrl()), new RequestExecution(config.target.buildRootUrl())){
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
				},
				RestTemplateBuilder.build(requireNonNull(config.refer)),
				RestTemplateBuilder.build(requireNonNull(config.target)));

		try {
			assertions.assertApi(request.getRequest());
			log.info("TEST {} OK", request);
		}
		catch(Throwable e) {
			//fail
			log.error("assertion fail", e);
		}
		responseComparator.setAct(actualResponse);
		responseComparator.setExp(expectedResponse);
		return responseComparator;
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
	}
}
