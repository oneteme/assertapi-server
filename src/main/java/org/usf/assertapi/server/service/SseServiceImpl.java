package org.usf.assertapi.server.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.usf.assertapi.server.model.AssertionEvent;
import org.usf.assertapi.server.model.AssertionResult;
import org.usf.assertapi.server.model.ExecutionState;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static java.util.Map.entry;
import static org.usf.assertapi.server.model.ExecutionState.ABORTED;
import static org.usf.assertapi.server.model.ExecutionState.DONE;

@Slf4j
@RequiredArgsConstructor
public final class SseServiceImpl implements SseService {

    private static final Map<Long, Map.Entry<SseEmitter, AssertionEvent>> sseEmitters = new ConcurrentHashMap<>();
    private final BiConsumer<Long, ExecutionState> action;

    @Override
    public SseEmitter subscribe(long id) {
        return sseEmitters.get(id).getKey();
    }
    
    @Override
    public void init(long id) {
        sseEmitters.put(id, entry(newSseEmitter(id), new AssertionEvent(0)));
    }

    @Override
    public void start(long id, AssertionEvent assertionEvent) {
    	SseEmitter sse = sseEmitters.containsKey(id) 
    			? sseEmitters.remove(id).getKey()
    			: newSseEmitter(id);
        sseEmitters.put(id, entry(sse, assertionEvent));
        safeSend(sse, assertionEvent);
    }

    @Override
    public void update(long id, @NonNull AssertionResult result) {
        var entry = sseEmitters.get(id);
        if(entry != null) {
            var assertionResult = entry.getValue();
            assertionResult.append(result);
            var sse = entry.getKey();
            safeSend(sse, assertionResult);
            if(assertionResult.isComplete()) {
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
        action.accept(id, o.getValue().isComplete() ? DONE : ABORTED);
    }
    
    private static void safeSend(SseEmitter sse, AssertionEvent assertionEvent) {
        try {
            sse.send(assertionEvent);
        } catch (IOException e) {
            log.error("server sent events fail", e);
        }
    }
}
