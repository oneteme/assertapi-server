package org.usf.assertapi.server.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.of;
import static org.springframework.http.ResponseEntity.ok;
import static org.usf.assertapi.core.AssertionEnvironement.buildContext;
import static org.usf.assertapi.core.AssertionEnvironement.from;
import static org.usf.assertapi.server.model.ApiTraceStatistic.from;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.usf.assertapi.core.ApiAssertionFactory;
import org.usf.assertapi.core.ApiDefaultAssertion;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.JsonResponseCompareConfig;
import org.usf.assertapi.core.ResponseProxyComparator;
import org.usf.assertapi.core.RestTemplateBuilder;
import org.usf.assertapi.core.ServerConfig;
import org.usf.assertapi.core.TestStatus;
import org.usf.assertapi.core.TestStep;
import org.usf.assertapi.server.model.ApiRequestGroupServer;
import org.usf.assertapi.server.model.ApiRequestServer;
import org.usf.assertapi.server.model.ApiResponseServer;
import org.usf.assertapi.server.service.RequestService;
import org.usf.assertapi.server.service.SseService;
import org.usf.assertapi.server.service.SseServiceImpl;
import org.usf.assertapi.server.service.TraceService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/v1/assert/api")
public class MainController {

	private final RequestService requestService;
	private final TraceService traceService;
	private final RequestController requestController;
	private final SseService sseService;

	public MainController(RequestService requestService, TraceService traceService, RequestController requestController) {
		this.requestService = requestService;
		this.traceService = traceService;
		this.requestController = requestController;
		this.sseService = new SseServiceImpl(traceService::updateStatus);
	}

	//TODO change it to PathParam (ctx=>id)
	@GetMapping("subscribe")
	public SseEmitter subscribe(@RequestParam(name="ctx") long ctx) {
		return sseService.subscribe(ctx);
	}

	@GetMapping("load")
	public ResponseEntity<List<ApiRequest>> load(
			@RequestHeader Map<String, String> headers,
			@RequestParam(name="app") String app,
			@RequestParam(name="stable_release") String stableRelease) {
		
		var ctx = from(headers::get);
		var id = traceService.register(ctx, app, ctx.getAddress(), stableRelease); //latest <= dev machine
		var list = requestController.get(null, app, singletonList(stableRelease));
		sseService.start(id, from(list));
		HttpHeaders hdr = new HttpHeaders();
		hdr.set("trace", "/v1/assert/api/trace/" + id);
		return ok().headers(hdr).body(list);
	}

	@PostMapping("run")
	public long run(
			@RequestParam(name="app") String app,
			@RequestParam(name="latest_release") String latestRelease,
			@RequestParam(name="stable_release") String stableRelease,
			@RequestParam(name="id", required = false) int[] ids,
			@RequestParam(name="excluded", required = false) boolean excluded,
			@RequestBody Configuration config) {
		
		var id = traceService.register(buildContext().withUser("front_user"), app, latestRelease, stableRelease);
		sseService.init(id);
		
		new ApiAssertionFactory()
		.comparing(config.getRefer(), config.getTarget())
		.trace(a-> {
			traceService.addTrace(id, a);
			sseService.update(id, a);
		})
		.build()
		.assertAllAsync(()->{
			var list = requests(app, latestRelease, stableRelease, ids, excluded);
			sseService.start(id, from(list));
			return list.stream();
		});
		return id;
	}
	
	@Deprecated // TODO à refaire
	private List<ApiRequest> requests(String app, String latestRelease, String stableRelease, int[] ids, boolean excluded) {

		List<String> envs = asList(latestRelease, stableRelease);
		var list = requestController.getAll(!excluded ? ids : null, app, envs).stream()
				.filter(r -> r.getRequestGroupList().stream().map(ApiRequestGroupServer::getEnv).collect(toList()).containsAll(envs))
				.map(ApiRequestServer::getRequest)
				.collect(toList());
		if(excluded && ids != null) { //TODO else ? 
			of(ids).forEach(i-> list.stream().filter(t-> t.getId() == i).findAny().ifPresent(t-> t.getExecConfig().disable()));
		}
		return list;
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
		var assertions = new ApiDefaultAssertion(
				new ResponseProxyComparator(new org.usf.assertapi.core.ResponseComparator(), t -> {responseComparator.setStatus(t.getStatus()); responseComparator.setStep(t.getStep());}){
					@Override
					public void assertJsonContent(String expectedContent, String actualContent, JsonResponseCompareConfig strict) {
						expectedResponse.setResponse(expectedContent);
						actualResponse.setResponse(actualContent);
						super.assertJsonContent(expectedContent, actualContent, strict);
					}

					@Override
					public void assertContentType(String expectedContentType, String actualContentType) {
						expectedResponse.setContentType(expectedContentType);
						actualResponse.setContentType(actualContentType);
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
			assertions.assertAll(request.getRequest());
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
