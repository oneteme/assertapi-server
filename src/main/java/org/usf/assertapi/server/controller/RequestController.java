package org.usf.assertapi.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.server.service.RequestService;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api/request")
public class RequestController {
    
	private final RequestService service;

    @GetMapping
    public List<ApiRequest> get(
            @RequestParam(name="id", required = false) int[] ids,
            @RequestParam(name="app", required = false) String app,
            @RequestParam(name="env", required = false) List<String> envs) {
        return service.getRequestList(ids, app, envs != null ? envs : new ArrayList<>());
    }

    @PutMapping //TODO put env + app in path no => delete  ApiRequestServer
    public long put(
            @RequestParam(name="app") String app,
            @RequestParam(name="env") List<String> envs,
            @RequestBody ApiRequest query
    ) {
        return service.addRequest(app, envs, query);
    }

    @PostMapping //TODO put env + app in path no => delete  ApiRequestServer & no need to couple 
    public void update(
            @RequestParam(name="app") String app,
            @RequestParam(name="env") List<String> envs,
            @RequestBody ApiRequest query) {
        service.updateRequest(app, envs, query);
    }

    @DeleteMapping
    public void delete(@RequestParam("id") int[] ids) {
        service.removeRequest(ids);
    }

    @PatchMapping("enable")
    public void enable(@RequestParam("id") int[] ids) {
        service.updateState(ids, true);
    }

    @PatchMapping("disable")
    public void disable(@RequestParam("id") int[] ids) {
        service.updateState(ids, false);
    }

}
