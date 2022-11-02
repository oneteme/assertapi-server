package org.usf.assertapi.server.service;

import org.usf.assertapi.server.model.ApiServerConfig;

public interface EnvironmentService {
    long addEnvironment(ApiServerConfig serverConfig);

    void updateEnvironment(ApiServerConfig serverConfig);
}
