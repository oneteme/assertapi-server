package org.usf.assertapi.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.core.AssertionContext;
import org.usf.assertapi.server.dao.TraceDao;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TraceServiceImpl implements TraceService {
    private final TraceDao dao;

    @Override
    public List<ApiAssertionsResultServer> getTraces(long[] ids) {
        return dao.select(ids);
    }

    @Override
    public void addTrace(long id, ApiAssertionsResult res) {
        dao.insert(id, res);
    }

    @Override
    public long register(AssertionContext ctx, String app, String actEnv, String expEnv) {
        return dao.register(ctx, app, actEnv, expEnv);
    }

    @Override
    public List<ApiTraceGroup> getTraceGroups() {
        return dao.selectTraceGroup();
    }
}
