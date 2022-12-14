package org.usf.assertapi.server.dao;

import java.util.List;

import org.usf.assertapi.core.AssertionContext;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import lombok.NonNull;

public interface TraceDao {
    List<ApiAssertionsResultServer> select(long[] ids, List<String> status);

    void insert(long id, AssertionResult res);

    long register(AssertionContext ctx, String app, String actEnv, String expEnv, TraceGroupStatus status);

    List<ApiTraceGroup> selectTraceGroup(Long id);

    void updateStatus(@NonNull long id, TraceGroupStatus status);
}
