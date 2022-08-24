package org.usf.assertapi.server;

import java.util.List;

import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.AssertionContext;

public interface DataPersister {

	List<ApiRequest> data(String app, String env);

	void insert(String app, String env, ApiRequest query);

	void state(int[] id, boolean state);

	void delete(int[] id);

	long register(AssertionContext ctx);

	void trace(long id, ApiAssertionsResult list);

}