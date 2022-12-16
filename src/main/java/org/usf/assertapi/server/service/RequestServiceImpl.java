package org.usf.assertapi.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.server.dao.RequestDao;
import org.usf.assertapi.server.exception.NotFoundException;
import org.usf.assertapi.server.exception.TooManyResultException;
import org.usf.assertapi.server.model.ApiRequestServer;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestDao dao;

    @Override
    public List<ApiRequestServer> getRequestList(int[] ids, List<String> envs, String app) {
        return dao.selectRequest(ids, envs, app);
    }

    @Override
    public ApiRequestServer getRequestOne(int id) {
        int[] ids = {id};
        var requests = getRequestList(ids, null, null);
        if(requests == null || requests.isEmpty()) {
            throw new NotFoundException();
        } else if (requests.size() > 1) {
            throw new TooManyResultException(); 
        }
        return requests.iterator().next();
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRequest(int[] ids){
        dao.deleteRequest(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(int[] ids, boolean state){
        dao.updateState(ids, state);
    }
}
