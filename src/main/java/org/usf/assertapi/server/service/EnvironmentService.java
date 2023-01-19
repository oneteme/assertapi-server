package org.usf.assertapi.server.service;

import java.util.List;

import org.usf.assertapi.server.model.Environment;

public interface EnvironmentService {
	
    List<Environment> getEnvironments();

    long addEnvironment(Environment serverConfig);

    void updateEnvironment(int id, Environment serverConfig);

    void removeEnvironment(int[] ids);
}
