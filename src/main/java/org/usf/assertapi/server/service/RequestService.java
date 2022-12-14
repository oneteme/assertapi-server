package org.usf.assertapi.server.service;

import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.server.model.ApiRequestServer;

import java.util.List;

public interface RequestService {
    List<ApiRequestServer> getRequestList(int[] ids, List<String> envs, String app);

    ApiRequestServer getRequestOne(int id);

    long addRequest(ApiRequestServer req);

    void updateRequest(ApiRequestServer req);

    @Transactional(rollbackFor = Exception.class)
    void removeRequest(int[] ids);

    @Transactional(rollbackFor = Exception.class)
    void updateState(int[] ids, boolean state);
}
