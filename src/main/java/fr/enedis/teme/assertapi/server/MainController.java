package fr.enedis.teme.assertapi.server;

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
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import fr.enedis.teme.assertapi.core.ApiAssertionsFactory;
import fr.enedis.teme.assertapi.core.ApiAssertionsResult;
import fr.enedis.teme.assertapi.core.HttpQuery;
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
@RequestMapping("/v1/test/api")
public class MainController {
	
	private final DataPersister service;

	@RequestMapping
	public List<HttpQuery> queries(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env) {
		
		return service.data(app, env);
	}
	
	@PostMapping
	public void query(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env,
			@RequestBody HttpQuery query) {
		
		service.insert(app, env, query.build());
	}
	
	@PatchMapping("enable")
	public void enable(@RequestParam("id") int[] ids) {
		service.state(ids, true);
	}
	
	@PatchMapping("disable")
	public void disable(@RequestParam("id") int[] ids) {
		service.state(ids, false);
	}
	
	@DeleteMapping("delete")
	public void delete(@RequestParam("id") int[] ids) {
		service.delete(ids);
	}

	@PutMapping("trace")
	public void trace(@RequestBody ApiAssertionsResult result) {
		service.trace(singleton(result));
	}
	
	
	@PostMapping("run")
	public Object run(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env,
			@RequestParam(name="trace", defaultValue = "true") boolean trace,
			@RequestBody Configuration config) {
		
		List<ApiAssertionsResult> res = new LinkedList<>();
		
		var list = queries(app, env);
		var assertions = new ApiAssertionsFactory()
				.comparing(config.getRefer(), config.getTarget())
				.using(new DefaultResponseComparator())
				.trace(res::add)
				.build();
		for(var q : list) {
			try {
				assertions.assertApi(q);
				log.info("TEST {} OK", q);
			}
			catch(Exception e) {
				//fail
				log.error("assertion fail", e);
			}
		}
		try {//silent trace
			service.trace(res);
		}
		catch(Exception e) {
			log.error("error while saving results", e);
		}
		return res.stream()
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
	@JsonInclude(Include.NON_NULL)
	@RequiredArgsConstructor
	public static final class Result {

		private final String uri;
		private final TestStatus status;
		private final TestStep step;
		
		public static Result of(ApiAssertionsResult r) {
			return new Result(r.getQuery().getActual().toString(), r.getStatus(), r.getStep());
		}
		
	}
	
}
