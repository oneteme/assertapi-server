package org.usf.assertapi.server.service;

import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.server.model.ApiServerConfig;

import java.util.List;

public interface EnvironmentService {
    List<ApiServerConfig> getEnvironments();

    long addEnvironment(ApiServerConfig serverConfig);

    void updateEnvironment(ApiServerConfig serverConfig);

    void removeEnvironment(int[] ids);
}
