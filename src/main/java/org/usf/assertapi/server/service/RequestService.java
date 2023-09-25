package org.usf.assertapi.server.service;

import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.server.model.ApiRequestServer;

import java.util.List;
import java.util.Set;

public interface RequestService {
    List<ApiRequest> getRequestList(int[] ids, String app, Set<String> envs);

    List<ApiRequestServer> getRequestList();

    ApiRequest getRequestOne(int id);

    long addRequest(String app, List<String> releases, ApiRequest req);

    long[] addRequestList(String app, List<String> releases, List<ApiRequest> requests);

    void updateRequest(int id, String app, List<String> releases, ApiRequest req);

    void removeRequest(int[] ids);

    void updateState(int[] ids, boolean state);
}
