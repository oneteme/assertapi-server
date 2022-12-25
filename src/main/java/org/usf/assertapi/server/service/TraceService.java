package org.usf.assertapi.server.service;

import java.util.List;

import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.core.ApiCompareResult;
import org.usf.assertapi.server.model.AssertionResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import lombok.NonNull;

public interface TraceService {

    List<AssertionResultServer> getTraces(long[] ids, List<String> status);

    void addTrace(long idAsr, Long idReq, @NonNull ApiCompareResult res);

    long register(RuntimeEnvironement ctx, String app, String latestRelease, String stableRelease);

    void updateStatus(long id, TraceGroupStatus status);

    List<ApiTraceGroup> getTraceGroups(Long id);
}
