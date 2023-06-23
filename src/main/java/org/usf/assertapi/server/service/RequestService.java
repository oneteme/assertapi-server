package org.usf.assertapi.server.service;

import org.usf.assertapi.core.ApiRequest;

import java.util.List;

public interface RequestService {
    List<ApiRequest> getRequestList(int[] ids, String app, List<String> envs);

    ApiRequest getRequestOne(int id);

    long addRequest(String app, List<String> releases, ApiRequest req);

    long[] addRequestList(String app, List<String> releases, List<ApiRequest> requests);

    void updateRequest(int id, String app, List<String> releases, ApiRequest req);

    void removeRequest(int[] ids);

    void updateState(int[] ids, boolean state);
}
