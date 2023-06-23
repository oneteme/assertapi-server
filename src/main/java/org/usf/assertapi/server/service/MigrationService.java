package org.usf.assertapi.server.service;

import org.usf.assertapi.server.model.ApiMigration;

public interface MigrationService {
    long addMigration(ApiMigration migration);

    long updateMigration(int id, ApiMigration migration);
}
