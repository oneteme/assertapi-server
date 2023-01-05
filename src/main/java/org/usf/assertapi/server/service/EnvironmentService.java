package org.usf.assertapi.server.service;

import java.util.List;

import org.usf.assertapi.server.model.ApiEnvironment;

public interface EnvironmentService {
	
    List<ApiEnvironment> getEnvironments();

    long addEnvironment(ApiEnvironment serverConfig);

    void updateEnvironment(long id, ApiEnvironment serverConfig);

    void removeEnvironment(long[] ids);
}
