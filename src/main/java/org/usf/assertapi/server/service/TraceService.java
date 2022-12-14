package org.usf.assertapi.server.service;

import java.util.List;

import org.usf.assertapi.core.AssertionContext;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

public interface TraceService {

    List<ApiAssertionsResultServer> getTraces(long[] ids, List<String> status);

    void addTrace(long id, AssertionResult res);

    long register(AssertionContext ctx, String app, String actEnv, String expEnv, TraceGroupStatus status);

    void updateStatus(long id, TraceGroupStatus status);

    List<ApiTraceGroup> getTraceGroups(Long id);
}
