package org.usf.assertapi.server.dao;

import lombok.NonNull;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.core.RuntimeEnvironement;
import org.usf.assertapi.server.model.ApiTrace;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.TraceGroupStatus;

import java.util.List;

public interface TraceDao {
	
    List<ApiTrace> select(long[] ids, List<String> status);

    void insert(long idAsr, Long idReq, ComparisonResult res);

    long register(String app, String stableRelease, String latestRelease, RuntimeEnvironement ctx, TraceGroupStatus status);

    List<ApiTraceGroup> selectTraceGroup(Long id);

    void updateStatus(long id, TraceGroupStatus status);
}
