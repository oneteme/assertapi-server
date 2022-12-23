package org.usf.assertapi.server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.AssertionResultServer;
import org.usf.assertapi.server.service.TraceService;

import lombok.RequiredArgsConstructor;

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

    @PutMapping("{id}")
    public void put(@PathVariable("id") long id, @RequestBody AssertionResult result) {
        service.addTrace(id, result);
    }

    @GetMapping("group")
    public List<ApiTraceGroup> get(
            @RequestParam(name="id", required = false) Long id
    ) {
        return service.getTraceGroups(id);
    }
}
