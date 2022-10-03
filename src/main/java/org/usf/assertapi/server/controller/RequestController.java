package org.usf.assertapi.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.server.dao.RequestDao;
import org.usf.assertapi.server.model.ApiRequestServer;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api/request")
public class RequestController {
    private final RequestDao dao;

    @GetMapping
    public List<ApiRequest> get(
            @RequestParam(name="id", required = false) int[] ids,
            @RequestParam(name="app", required = false) String app,
            @RequestParam(name="env", required = false) String env) {
        return getAll(ids, app, env).stream().map(ApiRequestServer::getRequest).collect(Collectors.toList());
    }

    @PutMapping
    public void put(
            @RequestParam(name="app", required = false) String app,
            @RequestParam(name="env", required = false) String env,
            @RequestBody ApiRequest query) {
        dao.insert(app, env, query);
    }

    @DeleteMapping
    public void delete(@RequestParam("id") int[] ids) {
        dao.delete(ids);
    }

    @GetMapping("all")
    public List<ApiRequestServer> getAll(
            @RequestParam(name="id", required = false) int[] ids,
            @RequestParam(name="app", required = false) String app,
            @RequestParam(name="env", required = false) String env) {
        return dao.select(ids, app, env);
    }

    @PatchMapping("enable")
    public void enable(@RequestParam("id") int[] ids) {
        dao.updateState(ids, true);
    }

    @PatchMapping("disable")
    public void disable(@RequestParam("id") int[] ids) {
        dao.updateState(ids, false);
    }

}
