package org.usf.assertapi.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.server.model.ApiTrace;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.service.TraceService;

import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/non_regression/trace")
public class TraceController {
    
	private final TraceService service;

    @GetMapping
    public List<ApiTrace> get(@RequestParam(name="id", required = false) long[] ids,
                              @RequestParam(name = "status", required = false) List<String> status) {
        return service.getTraces(ids, status);
    }

    @PutMapping("{asrID}/api/{reqID}")
    public void put(@PathVariable("asrID") long idAsr,
                    @PathVariable("reqID") long idReq,
                    @RequestBody ComparisonResult result) {
        service.addTrace(idAsr, idReq, result);
    }

    @GetMapping("{asrID}/group")
    public List<ApiTraceGroup> get(@PathVariable(name="asrID", required = false) long id) {
        return service.getTraceGroups(id);
    }
}
