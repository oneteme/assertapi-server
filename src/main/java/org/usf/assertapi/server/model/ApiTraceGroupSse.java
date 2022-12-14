package org.usf.assertapi.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ApiTraceGroupSse {
    private final SseEmitter sseEmitter;
    private ApiTraceGroup apiTraceGroup;
}
