package org.usf.assertapi.server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.server.model.Environment;
import org.usf.assertapi.server.service.EnvironmentService;

import lombok.RequiredArgsConstructor;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/model/environment")
public class EnvironmentController {
	
    private final EnvironmentService service;

    @GetMapping
    public List<Environment> get() {
        return service.getEnvironments();
    }

    @PutMapping
    public long put(@RequestBody Environment serverConfig) {
        return service.addEnvironment(serverConfig);
    }

    @PostMapping("{id}")
    public void update(
            @PathVariable("id") int id,
            @RequestBody Environment serverConfig) {
        service.updateEnvironment(id, serverConfig);
    }

    @DeleteMapping
    public void delete(@RequestParam("id") int[] ids) {
        service.removeEnvironment(ids);
    }
}
