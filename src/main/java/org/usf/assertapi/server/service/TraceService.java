package org.usf.assertapi.server.service;

import lombok.NonNull;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.server.model.ApiTrace;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import java.util.List;

public interface TraceService {

    List<ApiTrace> getTraces(long[] ids, List<String> status);

    void addTrace(long idAsr, Long idReq, @NonNull ComparisonResult res);

    long register(String app, String latestRelease, String stableRelease, RuntimeEnvironement env);

    void updateStatus(long id, TraceGroupStatus status);

    List<ApiTraceGroup> getTraceGroups(Long id);
}
