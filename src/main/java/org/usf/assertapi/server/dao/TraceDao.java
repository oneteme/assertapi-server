package org.usf.assertapi.server.dao;

import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.core.AssertionContext;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import java.util.List;

public interface TraceDao {
    List<ApiAssertionsResultServer> select(long[] ids, List<String> status);

    void insert(long id, ApiAssertionsResult res);

    long register(AssertionContext ctx, String app, String actEnv, String expEnv, TraceGroupStatus status);

    List<ApiTraceGroup> selectTraceGroup(Long id);

    void updateStatus(@NonNull long id, TraceGroupStatus status);
}
