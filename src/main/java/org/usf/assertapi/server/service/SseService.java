package org.usf.assertapi.server.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.ApiTraceGroup;

public interface SseService {
    SseEmitter init(long id);

    void start(long id, ApiTraceGroup apiTraceGroup);

    void update(long id, AssertionResult result);

    SseEmitter subscribe(long id);

    void complete(long id);
}
