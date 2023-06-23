package org.usf.assertapi.server.dao;

import org.usf.assertapi.core.ApiRequest;

import java.util.List;

public interface RequestDao {

    List<ApiRequest> selectRequest(int[] ids, String app, List<String> envs);

    void insertRequest(int id, ApiRequest req);

    void updateRequest(int id, ApiRequest req);

    void deleteRequest(int[] ids);

    void insertRequestGroup(int id, String app, List<String> releases);

    void deleteRequestGroup(int id);

    void updateState(int[] ids, boolean state);

    Integer nextId(String col, String table);
}
