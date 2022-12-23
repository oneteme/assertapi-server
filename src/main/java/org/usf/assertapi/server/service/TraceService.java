package org.usf.assertapi.server.service;

import java.util.List;

import org.usf.assertapi.core.AssertionEnvironement;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.AssertionResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

public interface TraceService {

    List<AssertionResultServer> getTraces(long[] ids, List<String> status);

    void addTrace(long id, AssertionResult res);

    long register(AssertionEnvironement ctx, String app, String latestRelease, String stableRelease);

    void updateStatus(long id, TraceGroupStatus status);

    List<ApiTraceGroup> getTraceGroups(Long id);
}
