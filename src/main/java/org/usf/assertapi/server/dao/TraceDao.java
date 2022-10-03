package org.usf.assertapi.server.dao;

import lombok.NonNull;
import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.core.AssertionContext;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;

import java.util.List;

public interface TraceDao {
    List<ApiAssertionsResultServer> select(long[] ids, String app, String env);

    void insert(long id, @NonNull ApiAssertionsResult res);

    long register(@NonNull AssertionContext ctx);
}
