package org.usf.assertapi.server.service;

import org.usf.assertapi.server.model.ApiRequestServer;

public interface RequestService {
    long addRequest(ApiRequestServer req);

    void updateRequest(ApiRequestServer req);
}
