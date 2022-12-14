package org.usf.assertapi.server.service;

import java.util.List;

import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.server.model.AssertionResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import lombok.NonNull;

public interface TraceService {

    List<AssertionResultServer> getTraces(long[] ids, List<String> status);

    void addTrace(long idAsr, Long idReq, @NonNull ComparisonResult res);

    long register(String app, String latestRelease, String stableRelease, RuntimeEnvironement env);

    void updateStatus(long id, TraceGroupStatus status);

    List<ApiTraceGroup> getTraceGroups(Long id);
}
