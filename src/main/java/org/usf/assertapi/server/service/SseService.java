package org.usf.assertapi.server.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.ApiTraceStatistic;

public interface SseService {

    SseEmitter subscribe(long id);

    void init(long id);
	
	void start(long id, ApiTraceStatistic apiTraceGroup);

    void update(long id, AssertionResult result);


}
