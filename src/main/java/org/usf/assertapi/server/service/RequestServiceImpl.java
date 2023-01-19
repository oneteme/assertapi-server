package org.usf.assertapi.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.server.dao.RequestDao;
import org.usf.assertapi.server.exception.NotFoundException;
import org.usf.assertapi.server.exception.TooManyResultException;
import org.usf.assertapi.server.model.ApiMigration;

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
    public long addRequest(String app, List<String> releases, ApiRequest req) {
        int nextId = dao.nextId("ID_REQ", "O_REQ"); //TODO db column in service
        dao.insertRequest(nextId, req);
        dao.insertRequestGroup(nextId, app, releases);
        return nextId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long[] addRequestList(String app, List<String> releases, List<ApiRequest> requests) {
        long[] ids = new long[requests.size()];
        for (int i = 0; i < requests.size(); i++) {
            ids[i] = addRequest(app, releases, requests.get(i));
        }
        return ids;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRequest(int id, String app, List<String> releases, ApiRequest req) {
        dao.updateRequest(id, req);
        dao.deleteRequestGroup(id);
        dao.insertRequestGroup(id, app, releases);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addMigration(ApiMigration migration) {
        int nextId = dao.nextId("ID_MIG", "O_REQ_MIG"); //TODO db column in service
        dao.insertMigration(nextId, migration);
        return nextId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long updateMigration(int id, ApiMigration migration) {
        int nextId = dao.nextId("ID_MIG", "O_REQ_MIG"); //TODO db column in service
        dao.updateMigration(nextId, migration);
        return nextId;
    }
}
