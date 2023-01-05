package org.usf.assertapi.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.server.dao.RequestDao;
import org.usf.assertapi.server.exception.NotFoundException;
import org.usf.assertapi.server.exception.TooManyResultException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestDao dao;

    @Override
    public List<ApiRequest> getRequestList(int[] ids, String app, List<String> envs) {
        return dao.selectRequest(ids, app, envs);
    }

    @Override
    public ApiRequest getRequestOne(int id) {
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
    public long addRequest(String app, List<String> envs, ApiRequest req) {
        long nextId = dao.nextId("ID_REQ", "API_REQ"); //TODO db column in service
        dao.insertRequest(nextId, req);
        dao.insertRequestGroup(nextId, app, envs);
        return nextId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRequest(String app, List<String> envs, ApiRequest req) {
        long id = req.getId();
        dao.updateRequest(req);
        dao.deleteRequestGroup(id);
        dao.insertRequestGroup(id, app, envs);
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
