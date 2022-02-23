package fr.enedis.teme.assertapi.server;

import java.util.Collection;
import java.util.List;

import fr.enedis.teme.assertapi.core.ApiAssertionsResult;
import fr.enedis.teme.assertapi.core.ApiRequest;
import fr.enedis.teme.assertapi.core.AssertionContext;
import lombok.NonNull;

public interface DataPersister {

	List<ApiRequest> data(String app, String env);

	void insert(String app, String env, ApiRequest query);

	void state(int[] id, boolean state);

	void delete(int[] id);

	long register(AssertionContext ctx);

	void traceAll(long id, @NonNull Collection<ApiAssertionsResult> list);

}