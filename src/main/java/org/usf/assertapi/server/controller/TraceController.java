package org.usf.assertapi.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.AssertionResultServer;
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

    @GetMapping
    public List<AssertionResultServer> get(
            @RequestParam(name="id", required = false) long[] ids,
            @RequestParam(name = "status", required = false) List<String> status
    ) {
        return service.getTraces(ids, status);
    }

    @PutMapping("{" + CTX_ID + "}")
    public void put(@PathVariable(CTX_ID) long ctx, @RequestBody AssertionResult result) {
        service.addTrace(ctx, result);
    }

    @GetMapping("group")
    public List<ApiTraceGroup> get(
            @RequestParam(name="id", required = false) Long id
    ) {
        return service.getTraceGroups(id);
    }
}
