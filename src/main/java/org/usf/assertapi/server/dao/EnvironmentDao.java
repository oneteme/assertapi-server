package org.usf.assertapi.server.dao;

import lombok.NonNull;
import org.usf.assertapi.server.model.ApiServerConfig;

import java.util.List;

public interface EnvironmentDao {
    List<ApiServerConfig> select();

    void insert(@NonNull ApiServerConfig serverConfig);

    void delete(@NonNull int[] ids);
}
