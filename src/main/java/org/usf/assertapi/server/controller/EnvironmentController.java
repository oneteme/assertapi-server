package org.usf.assertapi.server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.usf.assertapi.server.model.ApiServerConfig;
import org.usf.assertapi.server.service.EnvironmentService;

import lombok.RequiredArgsConstructor;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api/environment")
public class EnvironmentController {
	
    private final EnvironmentService service;

    @GetMapping
    public List<ApiServerConfig> get() {
        return service.getEnvironments();
    }

    @PutMapping
    public long put(@RequestBody ApiServerConfig serverConfig) {
        return service.addEnvironment(serverConfig);
    }

    @PostMapping
    public void update(@RequestBody ApiServerConfig serverConfig) { //TODO pathvariable for ID
        service.updateEnvironment(serverConfig);
    }

    @DeleteMapping
    public void delete(@RequestParam("id") int[] ids) {
        service.removeEnvironment(ids);
    }
}
