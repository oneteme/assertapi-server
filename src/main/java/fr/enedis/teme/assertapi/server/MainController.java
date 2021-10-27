package fr.enedis.teme.assertapi.server;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.enedis.teme.assertapi.core.ApiAssertionsFactory;
import fr.enedis.teme.assertapi.core.HttpQuery;
import lombok.RequiredArgsConstructor;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/test/api", produces = APPLICATION_JSON_VALUE)
public class MainController {
	
	private final MainService service;

	@RequestMapping
	public List<HttpQuery> queries(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env) {
		
		return service.data(app, env);
	}
	
	@PostMapping
	public void query(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env,
			@RequestBody HttpQuery query) {
		
		service.insert(app, env, query.build());
	}
	
	@PatchMapping("{id}/enable")
	public void enable(@PathVariable("id") int id) {
		service.state(new int[] {id}, true);
	}
	
	@PatchMapping("{id}/disable")
	public void disable(@PathVariable("id") int id) {
		service.state(new int[] {id}, false);
	}
	
	@DeleteMapping("{id}/delete")
	public void delete(@PathVariable("id") int id) {
		service.delete(new int[] {id});
	}

	
	@RequestMapping("run")
	public void run(
			@RequestParam(name="app", required = false) String app,
			@RequestParam(name="env", required = false) String env,
			@RequestParam(name="trace", defaultValue = "true") boolean trace) {
		
		var list = queries(app, env);
		var assertions = new ApiAssertionsFactory()
//				.comparing(null, null)
				.using(new DefaultResponseComparator())
				.build();
		for(var q : list) {
			try {
				assertions.assertApi(q);
				//sucess
			}
			catch(Exception e) {
				//fail
			}
		}
	}
	
}
