package fr.enedis.teme.assertapi.server;

import java.util.Collection;
import java.util.List;

import fr.enedis.teme.assertapi.core.ApiAssertionsResult;
import fr.enedis.teme.assertapi.core.ApiRequest;

public interface DataPersister {

	List<ApiRequest> data(String app, String env);

	void insert(String app, String env, ApiRequest query);

	void state(int[] id, boolean state);

	void delete(int[] id);

	void traceAll(Collection<ApiAssertionsResult> list);
}