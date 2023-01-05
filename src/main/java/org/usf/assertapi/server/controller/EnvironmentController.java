package org.usf.assertapi.server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.server.model.ApiEnvironment;
import org.usf.assertapi.server.service.EnvironmentService;

import lombok.RequiredArgsConstructor;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api/environment")
public class EnvironmentController {
	
    private final EnvironmentService service;

    @GetMapping
    public List<ApiEnvironment> get() {
        return service.getEnvironments();
    }

    @PutMapping
    public long put(@RequestBody ApiEnvironment serverConfig) {
        return service.addEnvironment(serverConfig);
    }

    @PostMapping("{id}")
    public void update(
            @PathVariable("id") long id,
            @RequestBody ApiEnvironment serverConfig) {
        service.updateEnvironment(id, serverConfig);
    }

    @DeleteMapping
    public void delete(@RequestParam("id") long[] ids) {
        service.removeEnvironment(ids);
    }
}
