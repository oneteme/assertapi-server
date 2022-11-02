package org.usf.assertapi.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.server.dao.EnvironmentDao;
import org.usf.assertapi.server.model.ApiServerConfig;
import org.usf.assertapi.server.service.EnvironmentService;

import java.util.List;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api/environment")
public class EnvironmentController {
    private final EnvironmentService service;
    private final EnvironmentDao dao;

    @GetMapping
    public List<ApiServerConfig> get() {
        return dao.selectEnvironment();
    }

    @PutMapping
    public long put(
            @RequestBody ApiServerConfig serverConfig
    ) {
        return service.addEnvironment(serverConfig);
    }

    @PostMapping
    public void update(
            @RequestBody ApiServerConfig serverConfig
    ) {
        service.updateEnvironment(serverConfig);
    }

    @DeleteMapping
    public void delete(
            @RequestParam("id") int[] ids
    ) {
        dao.deleteEnvironment(ids);
    }
}
