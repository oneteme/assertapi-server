package org.usf.assertapi.server.service;

import static org.usf.assertapi.server.model.TraceGroupStatus.PENDING;

import java.util.List;

import org.springframework.stereotype.Service;
import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.core.ApiCompareResult;
import org.usf.assertapi.server.dao.TraceDao;
import org.usf.assertapi.server.model.AssertionResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TraceServiceImpl implements TraceService {
    
	private final TraceDao dao;

    @Override
    public List<AssertionResultServer> getTraces(long[] ids, List<String> status) {
        return dao.select(ids, status);
    }

    @Override
    public void addTrace(long idAsr, Long idReq, @NonNull ApiCompareResult res) {
        dao.insert(idAsr, idReq, res);
    }

    @Override
    public long register(String app, String stableRelease, String latestRelease, RuntimeEnvironement env) {
        return dao.register(app, stableRelease, latestRelease, env, PENDING);
    }

    @Override
    public void updateStatus(long id, TraceGroupStatus status){ //TODO update must return new Object
        dao.updateStatus(id, status);
    }

    @Override
    public List<ApiTraceGroup> getTraceGroups(Long id) {
        return dao.selectTraceGroup(id);
    }
}
