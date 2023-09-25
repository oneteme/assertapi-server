package org.usf.assertapi.server.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.server.model.AssertionEvent;
import org.usf.assertapi.server.model.AssertionResult;

import java.util.List;

public interface SseService {

    SseEmitter subscribe(long id);

    void init(long id);
	
	void start(long id, AssertionEvent assertionEvent);

    void update(long id, AssertionResult result);


}
