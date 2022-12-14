package org.usf.assertapi.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;
import org.usf.assertapi.server.service.SseService;
import org.usf.assertapi.server.service.TraceService;

import java.util.List;

import static org.usf.assertapi.core.AssertionContext.*;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api/trace")
public class TraceController {
    private final TraceService service;
    private final SseService sseService;
    private final ObjectMapper mapper;

    @GetMapping
    public List<ApiAssertionsResultServer> get(
            @RequestParam(name="id", required = false) long[] ids,
            @RequestParam(name = "status", required = false) List<String> status
    ) {
        return service.getTraces(ids, status);
    }

    @PutMapping
    public void put(@RequestHeader(CTX_ID) long ctx, @RequestBody AssertionResult result) {
        service.addTrace(ctx, result);
    }

    //TODO Required false for Ihm Header
    @GetMapping("register")
    public long register(
            @RequestHeader(value = CTX, required = false) String context,
            @RequestParam("app") String app,
            @RequestParam("actual_env") String actualEnv,
            @RequestParam("expected_env") String expectedEnv
    ) {
        var ctx = service.register(context != null ? parseHeader(mapper, context) : buildContext(), app, actualEnv, expectedEnv, TraceGroupStatus.PENDING);
        sseService.init(ctx);
        return ctx;
    }

    @GetMapping("group")
    public List<ApiTraceGroup> get(
            @RequestParam(name="id", required = false) Long id
    ) {
        return service.getTraceGroups(id);
    }
}
