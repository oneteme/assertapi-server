package org.usf.assertapi.server.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.usf.assertapi.core.AssertionContext;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.dao.TraceDao;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TraceServiceImpl implements TraceService {
    private final TraceDao dao;

    @Override
    public List<ApiAssertionsResultServer> getTraces(long[] ids, List<String> status) {
        return dao.select(ids, status);
    }

    @Override
    public void addTrace(long id, AssertionResult res) {
        dao.insert(id, res);
    }

    @Override
    public long register(AssertionContext ctx, String app, String actEnv, String expEnv, TraceGroupStatus status) {
        return dao.register(ctx, app, actEnv, expEnv, status);
    }

    @Override
    public void updateStatus(long id, TraceGroupStatus status){
        dao.updateStatus(id, status);
    }

    @Override
    public List<ApiTraceGroup> getTraceGroups(Long id) {
        return dao.selectTraceGroup(id);
    }
}
