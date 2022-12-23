package org.usf.assertapi.server.dao;

import java.util.List;

import org.usf.assertapi.core.AssertionEnvironement;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.AssertionResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import lombok.NonNull;

public interface TraceDao {
    List<AssertionResultServer> select(long[] ids, List<String> status);

    void insert(long id, AssertionResult res);

    long register(AssertionEnvironement ctx, String app, String latestRelease, String stableRelease, TraceGroupStatus status);

    List<ApiTraceGroup> selectTraceGroup(Long id);

    void updateStatus(@NonNull long id, TraceGroupStatus status);
}
