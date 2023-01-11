package org.usf.assertapi.server.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.usf.assertapi.core.*;
import org.usf.assertapi.server.model.ApiResponseServer;
import org.usf.assertapi.server.service.RequestService;
import org.usf.assertapi.server.service.SseService;
import org.usf.assertapi.server.service.SseServiceImpl;
import org.usf.assertapi.server.service.TraceService;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.of;
import static org.springframework.http.ResponseEntity.ok;
import static org.usf.assertapi.core.RuntimeEnvironement.build;
import static org.usf.assertapi.server.model.ApiTraceStatistic.from;

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

	@GetMapping("subscribe")
	public SseEmitter subscribe(@RequestParam(name="id") long id) {
		return sseService.subscribe(id);
	}

	@GetMapping("load")
	public ResponseEntity<List<ApiRequest>> load(
			@RequestHeader Map<String, String> headers,
			@RequestParam(name="app") String app,
			@RequestParam(name="stable_release") String stableRelease) {
		
		var env = RuntimeEnvironement.from(headers::get);
		var id = traceService.register(app, env.getAddress(), stableRelease, env); //latest <= dev machine
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
		
		var id = traceService.register(app, latestRelease, stableRelease, build().withUser("front_user"));
		sseService.init(id);
		
		new ApiAssertionFactory()
		.comparing(config.getRefer(), config.getTarget())
		.trace((r, e)-> {
			traceService.addTrace(id, r.getId(), e);
			sseService.update(id, e);
		})
		.build()
		.assertAllAsync(()->{
			var list = requests(app, latestRelease, stableRelease, ids, excluded);
			sseService.start(id, from(list));
			return list.stream();
		});
		return id;
	}

	private List<ApiRequest> requests(String app, String latestRelease, String stableRelease, int[] ids, boolean excluded) {

		List<String> envs = asList(latestRelease, stableRelease);
		var list = requestController.get(!excluded ? ids : null, app, envs);
		if(excluded && ids != null) { //TODO else ? 
			of(ids).forEach(i-> list.stream().filter(t-> t.getId() == i).findAny().ifPresent(t-> t.getExecutionConfig().disable()));
		}
		return list;
	}

	@PostMapping("run/{id}")
	public ResponseComparator run(
			@PathVariable("id") int id,
			@RequestBody Configuration config) {
		
		var responseComparator = new ResponseComparator();
		responseComparator.setAct(new ApiResponseServer());
		responseComparator.setExp(new ApiResponseServer());
		var request = requestService.getRequestOne(id);
		var assertions = new ApiDefaultAssertion(
				new ResponseComparatorProxy(new org.usf.assertapi.core.ResponseComparator(), null){
					@Override
					public void assertContentType(String expectedContentType, String actualContentType) {
						responseComparator.getExp().setContentType(expectedContentType);
						responseComparator.getAct().setContentType(actualContentType);
						super.assertContentType(expectedContentType, actualContentType);
					}

					@Override
					public void assertStatusCode(int expectedStatusCode, int actualStatusCode) {
						responseComparator.getExp().setStatusCode(expectedStatusCode);
						responseComparator.getAct().setStatusCode(actualStatusCode);
						super.assertStatusCode(expectedStatusCode, actualStatusCode);
					}
					
					@Override
					public void assertJsonContent(String expectedContent, String actualContent, ContentComparator<?> strict) {
						responseComparator.getExp().setResponse(expectedContent);
						responseComparator.getAct().setResponse(actualContent);
						super.assertJsonContent(expectedContent, actualContent, strict);
					}

					@Override
					public void finish(CompareStatus status) {
						responseComparator.setStatus(status); 
						responseComparator.setStep(getCurrentStage());
					}
				},
				RestTemplateBuilder.build(requireNonNull(config.refer)),
				RestTemplateBuilder.build(requireNonNull(config.target))
		);

		assertions.assertApi(request);
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
		private CompareStatus status;
		private CompareStage step;
	}
}
