package org.usf.assertapi.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.server.dao.TraceDao;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
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
    private final ObjectMapper mapper;

    @GetMapping
    public List<ApiAssertionsResultServer> get(
            @RequestParam(name="id", required = false) long[] ids
    ) {
        return service.getTraces(ids);
    }

    @PutMapping
    public void put(@RequestHeader(CTX_ID) long ctx, @RequestBody ApiAssertionsResult result) {
        service.addTrace(ctx, result);
    }

    @GetMapping("register")
    public long register(@RequestHeader(CTX) String context) {
        return service.register(parseHeader(mapper, context), null, null, null);
    }

    @GetMapping("group")
    public List<ApiTraceGroup> get() {
        return service.getTraceGroups();
    }
}
