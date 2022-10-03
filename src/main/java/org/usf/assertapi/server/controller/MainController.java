package org.usf.assertapi.server.controller;

import static org.usf.assertapi.core.AssertionContext.buildContext;

import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.usf.assertapi.core.ApiAssertionsFactory;
import org.usf.assertapi.core.ServerConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.usf.assertapi.server.dao.EnvironmentDao;
import org.usf.assertapi.server.dao.RequestDao;
import org.usf.assertapi.server.dao.TraceDao;
import org.usf.assertapi.server.model.ApiRequestServer;
import org.usf.assertapi.server.DefaultResponseComparator;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api")
public class MainController {

	private final TraceDao traceDao;
	private final RequestDao requestDao;
	
	@PostMapping("run")
	public long run(
			@RequestParam(name="id", required = false) int[] ids,
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env,
			@RequestBody Configuration config) {

		var ctx = traceDao.register(buildContext());
		var list = requestDao.select(ids, app, env).stream().map(ApiRequestServer::getRequest).collect(Collectors.toList());
		var assertions = new ApiAssertionsFactory()
				.comparing(config.getRefer(), config.getTarget())
				.using(new DefaultResponseComparator())
				.trace(a-> traceDao.insert(ctx, a))
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
		return ctx;
	}
	
	@Getter
	@RequiredArgsConstructor
	public static final class Configuration {
		
		private final ServerConfig refer;
		private final ServerConfig target;
	}
		
}
