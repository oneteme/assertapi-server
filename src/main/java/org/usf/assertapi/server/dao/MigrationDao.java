package org.usf.assertapi.server.dao;

import lombok.NonNull;
import org.usf.assertapi.server.model.ApiMigration;

public interface MigrationDao {
    void insertMigration(int id, @NonNull ApiMigration migration);

    void updateMigration(int id, @NonNull ApiMigration migration);

    Integer nextId(@NonNull String col, @NonNull String table);
}
