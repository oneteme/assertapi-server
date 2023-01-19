package org.usf.assertapi.server.dao;

import org.usf.assertapi.server.model.Environment;

import java.util.List;

public interface EnvironmentDao {
    List<Environment> selectEnvironment();

    void insertEnvironment(int id, Environment environment);

    void updateEnvironment(int id, Environment environment);

    void deleteEnvironment(int[] ids);

    Integer nextId(String col, String table);
}
