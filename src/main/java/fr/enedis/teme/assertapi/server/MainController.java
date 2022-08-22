package fr.enedis.teme.assertapi.server;

import static fr.enedis.teme.assertapi.core.AssertionContext.CTX;
import static fr.enedis.teme.assertapi.core.AssertionContext.CTX_ID;
import static fr.enedis.teme.assertapi.core.AssertionContext.buildContext;
import static fr.enedis.teme.assertapi.core.AssertionContext.parseHeader;

import java.util.LinkedList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.enedis.teme.assertapi.core.ApiAssertionsFactory;
import fr.enedis.teme.assertapi.core.ApiAssertionsResult;
import fr.enedis.teme.assertapi.core.ApiRequest;
import fr.enedis.teme.assertapi.core.ServerConfig;
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
	private final ObjectMapper mapper;

	@RequestMapping
	public List<ApiRequest> requests(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env) {
		return service.data(app, env);
	}
	
	@PutMapping
	public void request(
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
	
	@GetMapping("trace")
	public long register(@RequestHeader(CTX) String context) {
		return service.register(parseHeader(mapper, context));
	}
	
	@PutMapping("trace")
	public void trace(@RequestHeader(CTX_ID) long ctx, @RequestBody ApiAssertionsResult result) {
		service.trace(ctx, result);
	}
	
	@PostMapping("run")
	public List<ApiAssertionsResult> run(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env,
			@RequestBody Configuration config) {

		var ctx = service.register(buildContext());
		List<ApiAssertionsResult> results = new LinkedList<>();
		var list = requests(app, env);
		var assertions = new ApiAssertionsFactory()
				.comparing(config.getRefer(), config.getTarget())
				.using(new DefaultResponseComparator())
				.trace(a-> service.trace(ctx, a))
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
		return results;
	}
	
	@Getter
	@RequiredArgsConstructor
	public static final class Configuration {
		
		private final ServerConfig refer;
		private final ServerConfig target;
	}
		
}
