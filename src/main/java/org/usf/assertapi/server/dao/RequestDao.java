package org.usf.assertapi.server.dao;

import lombok.NonNull;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.server.model.ApiRequestServer;

import java.util.List;

public interface RequestDao {
    List<ApiRequestServer> select(int[] ids, String app, String env);

    void insert(String app, String env, @NonNull ApiRequest req);

    void delete(@NonNull int[] id);

    void updateState(@NonNull int[] id, boolean state);
}
