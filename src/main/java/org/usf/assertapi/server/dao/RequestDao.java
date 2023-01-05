package org.usf.assertapi.server.dao;

import org.usf.assertapi.core.ApiRequest;

import java.util.List;

public interface RequestDao {

    List<ApiRequest> selectRequest(int[] ids, String app, List<String> envs);

    void insertRequest(long id, ApiRequest req);

    void updateRequest(ApiRequest req);

    void deleteRequest(int[] ids);

    void insertRequestGroup(long id, String app, List<String> envs);

    void deleteRequestGroup(long id);

    void updateState(int[] ids, boolean state);

    Long nextId(String col, String table);
}
