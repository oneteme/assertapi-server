package fr.enedis.teme.assertapi.server;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Collections.singleton;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude;

import fr.enedis.teme.assertapi.core.ApiAssertionsFactory;
import fr.enedis.teme.assertapi.core.ApiAssertionsResult;
import fr.enedis.teme.assertapi.core.ApiRequest;
import fr.enedis.teme.assertapi.core.ServerConfig;
import fr.enedis.teme.assertapi.core.TestStatus;
import fr.enedis.teme.assertapi.core.TestStep;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api")
public class MainController {
	
	private final DataPersister service;

	@RequestMapping
	public List<ApiRequest> queries(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env) {
		
		return service.data(app, env);
	}
	
	@PutMapping
	public void query(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env,
			@RequestBody ApiRequest query) {
		
		service.insert(app, env, query);
	}
	
	@DeleteMapping
	public void delete(@RequestParam("id") int[] ids) {
		service.delete(ids);
	}
	
	@PatchMapping("enable")
	public void enable(@RequestParam("id") int[] ids) {
		service.state(ids, true);
	}
	
	@PatchMapping("disable")
	public void disable(@RequestParam("id") int[] ids) {
		service.state(ids, false);
	}
	
	@PutMapping("trace")
	public void trace(@RequestBody ApiAssertionsResult result) {
		service.traceAll(singleton(result));
	}
	
	
	@PostMapping("run")
	public Object run(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env,
			@RequestParam(name="trace", defaultValue = "true") boolean trace,
			@RequestBody Configuration config) {
		
		List<ApiAssertionsResult> results = new LinkedList<>();
		
		var list = queries(app, env);
		var assertions = new ApiAssertionsFactory()
				.comparing(config.getRefer(), config.getTarget())
				.using(new DefaultResponseComparator())
				.trace(results::add)
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
		if(trace) {
			try {
				service.traceAll(results);
			}//silent trace
			catch(Exception e) {
				log.error("error while tracing results", e);
			}
		}
		return results.stream()
				.map(Result::of)
				.collect(Collectors.toList());
	}
	
	@Getter
	@RequiredArgsConstructor
	public static final class Configuration {
		
		private final ServerConfig refer;
		private final ServerConfig target;
	}
	
	@Getter
	@JsonInclude(NON_NULL)
	@RequiredArgsConstructor
	public static final class Result {

		private final String uri;
		private final TestStatus status;
		private final TestStep step;
		
		public static Result of(ApiAssertionsResult r) {
			return new Result(
					r.getQuery().toString(), 
					r.getStatus(), 
					r.getStep());
		}
		
	}
	
}
