package org.usf.assertapi.server.persister;

import java.util.List;

import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.AssertionContext;
import org.usf.assertapi.server.model.ApiRequestServer;

public interface DataPersister {

	List<ApiRequestServer> data(String app, String env);

	void insert(String app, String env, ApiRequest query);

	void state(int[] id, boolean state);

	void delete(int[] id);

	long register(AssertionContext ctx);

	void trace(long id, ApiAssertionsResult list);

}