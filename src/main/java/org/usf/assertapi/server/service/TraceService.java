package org.usf.assertapi.server.service;

import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.core.AssertionContext;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;

import java.util.List;

public interface TraceService {

    List<ApiAssertionsResultServer> getTraces(long[] ids);

    void addTrace(long id, ApiAssertionsResult res);

    long register(AssertionContext ctx, String app, String actEnv, String expEnv);

    List<ApiTraceGroup> getTraceGroups();
}
