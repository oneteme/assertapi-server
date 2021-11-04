package fr.enedis.teme.assertapi.server;

import java.util.Collection;
import java.util.List;

import fr.enedis.teme.assertapi.core.ApiAssertionsResult;
import fr.enedis.teme.assertapi.core.HttpQuery;

public interface DataPersister {

	List<HttpQuery> data(String app, String env);

	void insert(String app, String env, HttpQuery query);

	void state(int[] id, boolean state);

	void delete(int[] id);

	void trace(Collection<ApiAssertionsResult> list);
}