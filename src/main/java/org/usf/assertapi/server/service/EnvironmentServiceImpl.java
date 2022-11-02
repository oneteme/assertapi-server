package org.usf.assertapi.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.server.dao.EnvironmentDao;
import org.usf.assertapi.server.model.ApiServerConfig;

@Service
@RequiredArgsConstructor
public class EnvironmentServiceImpl implements EnvironmentService {
    private final EnvironmentDao dao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addEnvironment(ApiServerConfig serverConfig) {
        long nextId = dao.nextId("ID_ENV", "API_ENV");
        dao.insertEnvironment(nextId, serverConfig);
        return nextId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEnvironment(ApiServerConfig serverConfig) {
        dao.updateEnvironment(serverConfig);
    }
}
