package org.usf.assertapi.server.dao;

import lombok.NonNull;
import org.usf.assertapi.server.model.ApiServerConfig;

import java.util.List;

public interface EnvironmentDao {
    List<ApiServerConfig> selectEnvironment();

    void insertEnvironment(long id, @NonNull ApiServerConfig serverConfig);

    void updateEnvironment(@NonNull ApiServerConfig serverConfig);

    void deleteEnvironment(@NonNull int[] ids);

    Long nextId(String col, String table);
}
