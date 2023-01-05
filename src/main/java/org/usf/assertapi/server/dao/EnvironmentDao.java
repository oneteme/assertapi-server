package org.usf.assertapi.server.dao;

import org.usf.assertapi.server.model.ApiEnvironment;

import java.util.List;

public interface EnvironmentDao {
    List<ApiEnvironment> selectEnvironment();

    void insertEnvironment(long id, ApiEnvironment serverConfig);

    void updateEnvironment(long id, ApiEnvironment serverConfig);

    void deleteEnvironment(long[] ids);

    Long nextId(String col, String table);
}
