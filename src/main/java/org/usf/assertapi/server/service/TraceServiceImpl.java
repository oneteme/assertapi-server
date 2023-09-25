package org.usf.assertapi.server.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.server.dao.TraceDao;
import org.usf.assertapi.server.model.AssertionExecution;
import org.usf.assertapi.server.model.AssertionResult;
import org.usf.assertapi.server.model.ExecutionState;

import java.util.List;

import static org.usf.assertapi.server.model.ExecutionState.PENDING;

@Service
@RequiredArgsConstructor
public class TraceServiceImpl implements TraceService {
    
	private final TraceDao dao;

    @Override
    public List<AssertionResult> get(long[] ids, List<String> status) {
        return dao.select(ids, status);
    }

    @Override
    public List<AssertionExecution> get(Long id) {
        return dao.select(id);
    }

    @Override
    public void addTrace(long idAsr, Long idReq, @NonNull ComparisonResult res) {
        dao.insert(idAsr, idReq, res);
    }

    @Override
    public long register(String app, String latestRelease, String stableRelease, RuntimeEnvironement env) {
        return dao.register(app, latestRelease, stableRelease, env, PENDING);
    }

    @Override
    public void updateStatus(long id, ExecutionState status){ //TODO update must return new Object
        dao.updateStatus(id, status);
    }


}
