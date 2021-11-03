package fr.enedis.teme.assertapi.server;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.enedis.teme.assertapi.core.ApiAssertionsFactory;
import fr.enedis.teme.assertapi.core.ApiAssertionsResult;
import fr.enedis.teme.assertapi.core.HttpQuery;
import fr.enedis.teme.assertapi.core.ServerConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/test/api")
public class MainController {
	
	private final MainService service;

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
	
	@PatchMapping("{id}/enable")
	public void enable(@PathVariable("id") int id) {
		service.state(new int[] {id}, true);
	}
	
	@PatchMapping("{id}/disable")
	public void disable(@PathVariable("id") int id) {
		service.state(new int[] {id}, false);
	}
	
	@DeleteMapping("{id}/delete")
	public void delete(@PathVariable("id") int id) {
		service.delete(new int[] {id});
	}

	@PutMapping("trace")
	public void trace(@RequestBody ApiAssertionsResult result) {
		service.trace(Instant.now(), result);
	}
	
	@PostMapping("run")
	public void run(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env,
			@RequestParam(name="trace", defaultValue = "true") boolean trace,
			@RequestBody Configuration config) {
		
		var list = queries(app, env);
		var assertions = new ApiAssertionsFactory()
				.comparing(config.getRefer(), config.getTarget())
				.using(new DefaultResponseComparator())
				.trace(this::trace)
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
	}
	
	
	@Getter
	@RequiredArgsConstructor
	public static final class Configuration {
		
		private final ServerConfig refer;
		private final ServerConfig target;
	}
	
}
