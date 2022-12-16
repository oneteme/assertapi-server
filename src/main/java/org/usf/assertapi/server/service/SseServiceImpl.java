package org.usf.assertapi.server.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.usf.assertapi.core.AssertionResult;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.ApiTraceGroupSse;
import org.usf.assertapi.server.model.TraceGroupStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    private final Map<Long, ApiTraceGroupSse> sseEmitters = new ConcurrentHashMap<>();
    private final BiConsumer<Long, TraceGroupStatus> statusConsumer;

    @Override
    public SseEmitter init(long id) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE); //TIMEOUT
        sseEmitter.onCompletion(() -> {
            sseEmitters.remove(id);
            statusConsumer.accept(id, TraceGroupStatus.FINISH);
        });
        sseEmitter.onTimeout(() -> {
            sseEmitters.remove(id);
            statusConsumer.accept(id, TraceGroupStatus.ABORTED);
        });
        sseEmitters.put(id, new ApiTraceGroupSse(sseEmitter));
        return sseEmitter;
    }

    @Override
    public void start(long id, ApiTraceGroup apiTraceGroup) {
        sseEmitters.get(id).setApiTraceGroup(apiTraceGroup);
        update(id, null);
    }

    @Override
    public void update(long id, AssertionResult result) {
        try {
            ApiTraceGroupSse apiTraceGroupSse = sseEmitters.get(id);
            if(apiTraceGroupSse != null) {
                ApiTraceGroup apiTraceGroup = apiTraceGroupSse.getApiTraceGroup();
                if(result != null) {
                    apiTraceGroup.append(result.getStatus());
                }
                sseEmitters.get(id).getSseEmitter().send(SseEmitter.event().name("result").data(apiTraceGroup));
            }
        } catch (IOException e) {
            log.error("server sent events fail", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public SseEmitter subscribe(long id) {
        return sseEmitters.get(id).getSseEmitter();
    }

    @Override
    public void complete(long id) {
        sseEmitters.get(id).getSseEmitter().complete();
    }
}
