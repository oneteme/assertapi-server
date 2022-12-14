package org.usf.assertapi.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.core.TestStatus;
import org.usf.assertapi.server.model.ApiTraceGroup;
import org.usf.assertapi.server.model.ApiTraceGroupSse;
import org.usf.assertapi.server.model.TraceGroupStatus;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    private final Map<Long, ApiTraceGroupSse> sseEmitters = new ConcurrentHashMap<>();
    private final TraceService traceService;

    @Override
    public SseEmitter init(long ctx) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE); //TIMEOUT
        sseEmitter.onCompletion(() -> {
            sseEmitters.remove(ctx);
            traceService.updateStatus(ctx, TraceGroupStatus.FINISH);
        });
        sseEmitter.onTimeout(() -> sseEmitters.remove(ctx));
        sseEmitters.put(ctx, new ApiTraceGroupSse(sseEmitter));
        return sseEmitter;
    }

    @Override
    public void start(long ctx, ApiTraceGroup apiTraceGroup) {
        sseEmitters.get(ctx).setApiTraceGroup(apiTraceGroup);
        update(ctx, null);
    }

    @Override
    public void update(long ctx, ApiAssertionsResult result) {
        try {
            ApiTraceGroupSse apiTraceGroupSse = sseEmitters.get(ctx);
            if(apiTraceGroupSse != null) {
                ApiTraceGroup apiTraceGroup = apiTraceGroupSse.getApiTraceGroup();
                if(result != null) {
                    apiTraceGroup.append(result.getStatus());
                }
                sseEmitters.get(ctx).getSseEmitter().send(SseEmitter.event().name("result").data(apiTraceGroup));
            }
        } catch (IOException e) {
            log.error("server sent events fail", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public SseEmitter subscribe(long ctx) {
        return sseEmitters.get(ctx).getSseEmitter();
    }

    @Override
    public void complete(long ctx) {
        sseEmitters.get(ctx).getSseEmitter().complete();
    }
}
