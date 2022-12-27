package org.usf.assertapi.server.dao;

import java.util.List;

import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.server.model.AssertionResultServer;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import lombok.NonNull;

public interface TraceDao {
	
    List<AssertionResultServer> select(long[] ids, List<String> status);

    void insert(long idAsr, Long idReq, @NonNull ComparisonResult res);

    long register(String app, String stableRelease, String latestRelease, RuntimeEnvironement ctx, TraceGroupStatus status);

    List<ApiTraceGroup> selectTraceGroup(Long id);

    void updateStatus(long id, TraceGroupStatus status);
}
