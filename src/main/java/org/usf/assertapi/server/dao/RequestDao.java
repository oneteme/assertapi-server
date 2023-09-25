package org.usf.assertapi.server.dao;

import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.server.model.ApiRequestServer;

import java.util.List;
import java.util.Set;

public interface RequestDao {

    List<ApiRequest> selectRequest(int[] ids, String app, Set<String> envs);

    List<ApiRequestServer> selectRequest();

    void insertRequest(int id, ApiRequest req);

    void updateRequest(int id, ApiRequest req);

    void deleteRequest(int[] ids);

    void insertRequestGroup(int id, String app, List<String> releases);

    void deleteRequestGroup(int[] ids);

    void updateState(int[] ids, boolean state);

    Integer nextId(String col, String table);
}
