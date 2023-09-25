package org.usf.assertapi.server.dao;

import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.server.model.AssertionExecution;
import org.usf.assertapi.server.model.AssertionResult;
import org.usf.assertapi.server.model.ExecutionState;

import java.util.List;

public interface TraceDao {
	
    List<AssertionResult> select(long[] ids, List<String> status);

    void insert(long idAsr, Long idReq, ComparisonResult res);

    long register(String app, String latestRelease, String stableRelease, RuntimeEnvironement ctx, ExecutionState status);

    List<AssertionExecution> select(Long id);

    void updateStatus(long id, ExecutionState status);
}
