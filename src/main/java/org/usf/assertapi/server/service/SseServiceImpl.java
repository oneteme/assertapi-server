package org.usf.assertapi.server.service;

import static java.util.Map.entry;
import static org.usf.assertapi.server.model.ApiTraceStatistic.NO_STAT;
import static org.usf.assertapi.server.model.ExecutionState.ABORTED;
import static org.usf.assertapi.server.model.ExecutionState.DONE;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.server.model.ApiTraceStatistic;
import org.usf.assertapi.server.model.ExecutionState;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class SseServiceImpl implements SseService {

    private static final Map<Long, Entry<ApiTraceStatistic, SseEmitter>> sseEmitters = new ConcurrentHashMap<>();
    private final BiConsumer<Long, ExecutionState> action;

    @Override
    public SseEmitter subscribe(long id) {
        return sseEmitters.get(id).getValue();
    }
    
    @Override
    public void init(long id) {
        sseEmitters.put(id, entry(NO_STAT, newSseEmitter(id)));
    }

    @Override
    public void start(long id, ApiTraceStatistic stat) {
    	SseEmitter sse = sseEmitters.containsKey(id) 
    			? sseEmitters.remove(id).getValue()
    			: newSseEmitter(id);
        sseEmitters.put(id, entry(stat, sse));
        safeSend(sse, stat);
    }

    @Override
    public void update(long id, @NonNull ComparisonResult result) {
        var entry = sseEmitters.get(id);
        if(entry != null) {
            var stat = entry.getKey();
            stat.append(result.getStatus());
            var sse = entry.getValue();
            safeSend(sse, stat);
            if(stat.isComplete()) {
            	sse.complete();
            }
        }
        else {
        	log.warn("can't find sseEmetter for id " + id);
        }
    }
    
    private SseEmitter newSseEmitter(long id) {
        SseEmitter sse = new SseEmitter(10 * 60 * 1000l); //TIMEOUT 10min
        sse.onCompletion(() -> complete(id));
        sse.onTimeout(() -> complete(id));
        return sse;
    }
    
    private void complete(long id) {
        var o = sseEmitters.remove(id);
        action.accept(id, o.getKey().isComplete() ? DONE : ABORTED);
    }
    
    private static void safeSend(SseEmitter sse, ApiTraceStatistic stat) {
        try {
            sse.send(stat);
        } catch (IOException e) {
            log.error("server sent events fail", e);
        }
    }
}
