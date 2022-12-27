package org.usf.assertapi.server.service;

import java.util.List;

import org.usf.assertapi.server.model.ApiServerConfig;

public interface EnvironmentService {
	
    List<ApiServerConfig> getEnvironments();

    long addEnvironment(ApiServerConfig serverConfig);

    void updateEnvironment(ApiServerConfig serverConfig);

    void removeEnvironment(int[] ids);
}
