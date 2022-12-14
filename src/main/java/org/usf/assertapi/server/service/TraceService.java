package org.usf.assertapi.server.service;

import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.core.AssertionContext;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import java.util.List;

public interface TraceService {

    List<ApiAssertionsResultServer> getTraces(long[] ids, List<String> status);

    void addTrace(long id, ApiAssertionsResult res);

    long register(AssertionContext ctx, String app, String actEnv, String expEnv, TraceGroupStatus status);

    void updateStatus(long id, TraceGroupStatus status);

    List<ApiTraceGroup> getTraceGroups(Long id);
}
