package org.usf.assertapi.server.dao;

import lombok.NonNull;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.server.model.ApiRequestGroupServer;
import org.usf.assertapi.server.model.ApiRequestServer;

import java.util.List;

public interface RequestDao {

    List<ApiRequestServer> selectRequest(int[] ids, List<String> envs, String app);

    void insertRequest(long id, @NonNull ApiRequest req);

    void insertRequestGroup(@NonNull long id, @NonNull List<ApiRequestGroupServer> requestGroupList);

    void deleteRequestGroup(@NonNull long id);

    void updateRequest(@NonNull ApiRequest req);

    void deleteRequest(@NonNull int[] id);

    void updateState(@NonNull int[] id, boolean state);

    Long nextId(String col, String table);
}
