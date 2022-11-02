package org.usf.assertapi.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.server.dao.RequestDao;
import org.usf.assertapi.server.model.ApiRequestServer;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestDao dao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addRequest(ApiRequestServer req) {
        long nextId = dao.nextId("ID_REQ", "API_REQ");
        dao.insertRequest(nextId, req.getRequest());
        dao.insertRequestGroup(nextId, req.getRequestGroupList());
        return nextId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRequest(ApiRequestServer req) {
        long id = req.getRequest().getId();
        dao.updateRequest(req.getRequest());
        dao.deleteRequestGroup(id);
        dao.insertRequestGroup(id, req.getRequestGroupList());
    }
}
