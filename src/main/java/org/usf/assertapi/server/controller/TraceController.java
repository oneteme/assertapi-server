package org.usf.assertapi.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.server.model.AssertionExecution;
import org.usf.assertapi.server.model.AssertionResult;
import org.usf.assertapi.server.service.TraceService;

import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/non_regression/trace")
public class TraceController {
    
	private final TraceService service;

    @GetMapping("assertion_result")
    public List<AssertionResult> get(@RequestParam(name="id", required = false) long[] ids,
                                     @RequestParam(name = "status", required = false) List<String> status) {
        return service.get(ids, status);
    }

    @GetMapping("assertion_execution")
    public List<AssertionExecution> get(@RequestParam(name="asrID", required = false) Long id) {
        return service.get(id);
    }

    @PutMapping("{asrID}/api/{reqID}")
    public void put(@PathVariable("asrID") long idAsr,
                    @PathVariable("reqID") long idReq,
                    @RequestBody ComparisonResult result) {
        service.addTrace(idAsr, idReq, result);
    }
}
