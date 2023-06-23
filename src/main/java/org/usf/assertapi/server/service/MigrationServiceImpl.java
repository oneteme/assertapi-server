package org.usf.assertapi.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.server.dao.MigrationDao;
import org.usf.assertapi.server.model.ApiMigration;

@Service
@RequiredArgsConstructor
public class MigrationServiceImpl implements MigrationService {
    private final MigrationDao dao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addMigration(ApiMigration migration) {
        int nextId = dao.nextId("ID_MIG", "O_REQ_MIG"); //TODO db column in service
        dao.insertMigration(nextId, migration);
        return nextId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long updateMigration(int id, ApiMigration migration) {
        int nextId = dao.nextId("ID_MIG", "O_REQ_MIG"); //TODO db column in service
        dao.updateMigration(nextId, migration);
        return nextId;
    }
}
