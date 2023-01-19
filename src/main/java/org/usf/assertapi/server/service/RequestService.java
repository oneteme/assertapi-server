package org.usf.assertapi.server.service;

import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.ContentComparator;
import org.usf.assertapi.server.model.ApiMigration;

import java.util.List;

public interface RequestService {
    List<ApiRequest> getRequestList(int[] ids, String app, List<String> envs);

    ApiRequest getRequestOne(int id);

    long addRequest(String app, List<String> releases, ApiRequest req);

    @Transactional(rollbackFor = Exception.class)
    long[] addRequestList(String app, List<String> releases, List<ApiRequest> requests);

    void updateRequest(int id, String app, List<String> releases, ApiRequest req);

    void removeRequest(int[] ids);

    void updateState(int[] ids, boolean state);

    long addMigration(ApiMigration migration);

    long updateMigration(int id, ApiMigration migration);
}
