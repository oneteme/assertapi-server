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
import org.usf.assertapi.server.model.ExecutionState;
import org.usf.assertapi.server.service.RequestService;
import org.usf.assertapi.server.service.SseService;
import org.usf.assertapi.server.service.SseServiceImpl;
import org.usf.assertapi.server.service.TraceService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.IntStream.of;
import static org.springframework.http.ResponseEntity.ok;
import static org.usf.assertapi.core.RuntimeEnvironement.build;

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
	public SseEmitter subscribe(@PathVariable(name="id") long id) {
		return sseService.subscribe(id);
	}

	@GetMapping("load")
	public ResponseEntity<List<ApiRequest>> load(
			@RequestHeader Map<String, String> headers,
			@RequestParam(name="app") String app,
			@RequestParam(name="stable_release") String stableRelease) {
		
		var env = RuntimeEnvironement.from(headers::get);
		var id = traceService.register(app, env.getAddress(), stableRelease, env); //latest <= dev machine
		var list = requestController.get(null, app, Set.of(stableRelease));
		// sseService.start(id, from(list));
		HttpHeaders hdr = new HttpHeaders();
		hdr.set("trace", "/v1/assert/api/trace/" + id);
		return ok().headers(hdr).body(list);
	}

	@GetMapping("register")
	public long register(
			@RequestParam(name="app") String app,
			@RequestParam(name="latest_release") String latestRelease,
			@RequestParam(name="stable_release") String stableRelease
	) {
		return traceService.register(app, latestRelease, stableRelease, build().withUser("front_user"));
	}

	@PostMapping("run")
	public long run(
			@RequestParam(name="app") String app,
			@RequestParam(name="latest_release") String latestRelease,
			@RequestParam(name="stable_release") String stableRelease,
			@RequestParam(name="id", required = false) int[] ids,
			@RequestParam(name="excluded", required = false) boolean excluded,
			@RequestBody Configuration config
	) {
		long id = traceService.register(app, latestRelease, stableRelease, build().withUser("front_user"));
		//sseService.init(id);
		/*new ApiAssertionFactory()
				.comparing(config.getLatestRelease(), config.getStableRelease())
				.trace((r, e)-> {
					traceService.addTrace(id, r.getId(), e);
					sseService.update(id, new AssertionResult(r.getId(), r.getName(), r.getDescription(), r.getUri(), r.getMethod(), e));
				})
				.build()
				.assertAsync(()->{
					var list = requests(app, latestRelease, stableRelease, ids, excluded);
					sseService.start(id, AssertionEvent.from(list));
					return list.stream();
				});*/

		new ApiAssertionFactory()
				.comparing(config.getStableRelease(), config.getLatestRelease())
				.trace((r, e)-> {
					traceService.addTrace(id, r.getId(), e);
				})
				.build()
				.assertAll(requests(app, latestRelease, stableRelease, ids, excluded).stream());
		traceService.updateStatus(id, ExecutionState.DONE);
		return id;
	}

	/*@PostMapping("run")
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
		.assertAsync(()->{
			var list = requests(app, latestRelease, stableRelease, ids, excluded);
			sseService.start(id, from(list));
			return list.stream();
		});
		return id;
	}*/

	private List<ApiRequest> requests(String app, String latestRelease, String stableRelease, int[] ids, boolean excluded) {

		Set<String> envs = new HashSet<>(asList(latestRelease, stableRelease));
		var list = requestController.get(!excluded ? ids : null, app, envs);
		if(excluded && ids != null) { //TODO else ? 
			of(ids).forEach(i-> list.stream().filter(t-> t.getId() == i).findAny().ifPresent(t-> t.getExecution().disable()));
		}
		return list;
	}

	@PostMapping("compare/{id}")
	public ResponseCompare run(
			@PathVariable("id") int id,
			@RequestBody Configuration config) {
		
		var runResponseComparator = new ResponseCompare();
		runResponseComparator.setAct(new ApiResponseServer());
		runResponseComparator.setExp(new ApiResponseServer());
		var request = requestService.getRequestOne(id);
		var assertions = new ResponseComparatorProxy(new ResponseComparator(), null) {
			@Override
			public void assertContentType(String expectedContentType, String actualContentType) {
				runResponseComparator.getExp().setContentType(expectedContentType);
				runResponseComparator.getAct().setContentType(actualContentType);
				super.assertContentType(expectedContentType, actualContentType);
			}
			@Override
			public void assertStatusCode(int expectedStatusCode, int actualStatusCode) {
				runResponseComparator.getExp().setStatusCode(expectedStatusCode);
				runResponseComparator.getAct().setStatusCode(actualStatusCode);
				super.assertStatusCode(expectedStatusCode, actualStatusCode);
			}

			@Override
			public void assertJsonContent(String expected, String actual, ModelComparator<?> config) {
				runResponseComparator.getExp().setResponse(expected);
				runResponseComparator.getAct().setResponse(actual);
				super.assertJsonContent(expected, actual, config);
			}

			@Override
			public void finish(ApiRequest request, ComparisonStatus status) {
				runResponseComparator.setStatus(status);
				runResponseComparator.setStep(getCurrentStage());
			}
		};
		try {
			new ApiAssertionFactory()
					.comparing(config.stableRelease, config.latestRelease)
					.using(assertions)
					.build()
					.assertApi(request);
		} catch(AssertionError e) {}
		return runResponseComparator;
	}

	@Getter
	@RequiredArgsConstructor
	public static final class Configuration {
		
		private final ServerConfig latestRelease;
		private final ServerConfig stableRelease;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static final class ResponseCompare {
		private ApiResponseServer exp;
		private ApiResponseServer act;
		private ComparisonStatus status;
		private ComparisonStage step;
	}
}
