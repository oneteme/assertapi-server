package org.usf.assertapi.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.server.model.ApiMigration;
import org.usf.assertapi.server.service.MigrationService;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/modelization/migration")
public class MigrationController {
    private final MigrationService service;


    //TODO GetMapping (ApiRequest or ApiMigration ?)

    @PutMapping
    public long put(
            @RequestBody ApiMigration migration
    ) {
        return service.addMigration(migration);
    }

    @PostMapping("{id}")
    public long update(
            @PathVariable("id") int id,
            @RequestBody ApiMigration migration
    ) {
        return service.updateMigration(id, migration);
    }
}
