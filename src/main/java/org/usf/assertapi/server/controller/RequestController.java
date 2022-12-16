package org.usf.assertapi.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.server.dao.RequestDao;
import org.usf.assertapi.server.model.ApiRequestServer;
import org.usf.assertapi.server.service.RequestService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        return getAll(ids, app, envs).stream().map(ApiRequestServer::getRequest).collect(Collectors.toList());
    }

    @PutMapping
    public long put(@RequestBody ApiRequestServer query) {

        return service.addRequest(query);
    }

    @PostMapping
    public void update(@RequestBody ApiRequestServer query) {
        service.updateRequest(query);
    }

    @DeleteMapping
    public void delete(@RequestParam("id") int[] ids) {
        service.removeRequest(ids);
    }

    @GetMapping("all")
    public List<ApiRequestServer> getAll(
            @RequestParam(name="id", required = false) int[] ids,
            @RequestParam(name="app", required = false) String app,
            @RequestParam(name="env", required = false) List<String> envs) {
        return service.getRequestList(ids, envs != null ? envs : new ArrayList<>(), app);
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
