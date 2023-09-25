package org.usf.assertapi.server.service;

import lombok.NonNull;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.server.model.AssertionExecution;
import org.usf.assertapi.server.model.AssertionResult;
import org.usf.assertapi.server.model.ExecutionState;

import java.util.List;

public interface TraceService {

    List<AssertionResult> get(long[] ids, List<String> status);

    List<AssertionExecution> get(Long id);

    void addTrace(long idAsr, Long idReq, @NonNull ComparisonResult res);

    long register(String app, String latestRelease, String stableRelease, RuntimeEnvironement env);

    void updateStatus(long id, ExecutionState status);

}
