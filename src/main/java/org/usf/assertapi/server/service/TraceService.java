package org.usf.assertapi.server.service;

import java.util.List;

import org.usf.assertapi.core.AssertionContext;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.AssertionResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

public interface TraceService {

    List<AssertionResultServer> getTraces(long[] ids, List<String> status);

    void addTrace(long id, AssertionResult res);

    long register(AssertionContext ctx, String app, String latestRelease, String stableRelease, TraceGroupStatus status);

    void updateStatus(long id, TraceGroupStatus status);

    List<ApiTraceGroup> getTraceGroups(Long id);
}
