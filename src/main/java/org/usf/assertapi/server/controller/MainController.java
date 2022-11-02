package org.usf.assertapi.server.controller;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.of;
import static org.usf.assertapi.core.AssertionContext.buildContext;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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
import org.usf.assertapi.server.dao.RequestDao;
import org.usf.assertapi.server.dao.TraceDao;
import org.usf.assertapi.server.model.ApiRequestGroupServer;
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
			@RequestParam(name="actual_env") String actualEnv,
			@RequestParam(name="expected_env") String expectedEnv,
			@RequestParam(name="app") String app,
			@RequestParam(name="id", required = false) int[] ids,
			@RequestParam(name="disabled_id", required = false) int[] disabledIds,
			@RequestBody Configuration config) {

		List<String> envs = asList(actualEnv, expectedEnv);
		var ctx = traceDao.register(buildContext());
		var list = requestDao.selectRequest(ids, envs, app).stream()
				.filter(r -> r.getRequestGroupList().stream().map(ApiRequestGroupServer::getEnv).collect(toList()).containsAll(envs))
				.map(ApiRequestServer::getRequest)
				.collect(toList());
		if(disabledIds != null) {
			of(disabledIds).forEach(i-> list.stream().filter(t-> t.getId() == i).findAny().ifPresent(t-> t.getConfiguration().setEnable(false)));
		}
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
