package cn.czh.service.impl;

import cn.czh.dto.NotifyMessage;
import cn.czh.service.ISseService;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseServiceImpl implements ISseService {

    private final Map<String, SseEmitter> sharedFilesEmitters = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> systemEventEmitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribeSharedFiles(String clientId) {
        SseEmitter emitter = new SseEmitter(0L); // 设置超时时间
        sharedFilesEmitters.put(clientId, emitter);

        emitter.onCompletion(() -> sharedFilesEmitters.remove(clientId));
        emitter.onTimeout(() -> sharedFilesEmitters.remove(clientId));
        return emitter;
    }

    @Override
    public SseEmitter subscribeSystemEvent(String clientId) {
        SseEmitter emitter = new SseEmitter(0L); // 设置超时时间
        systemEventEmitters.put(clientId, emitter);

        emitter.onCompletion(() -> systemEventEmitters.remove(clientId));
        emitter.onTimeout(() -> systemEventEmitters.remove(clientId));
        return emitter;
    }

    @Async
    @Override
    public void notifySharedFileUpdate(String message) {
        sharedFilesEmitters.forEach((clientId, emitter) ->
                sendNotification(clientId, emitter, "sharedFileUpdate", message, sharedFilesEmitters));
    }

    @Async
    @Override
    public void notifySystemEvent(NotifyMessage message) {
        systemEventEmitters.forEach((clientId, emitter) ->
                sendNotification(clientId, emitter, "systemEvent", JSONUtil.toJsonStr(message), systemEventEmitters));
    }

    private void sendNotification(String clientId, SseEmitter emitter, String event, String message, Map<String, SseEmitter> emittersMap) {
        try {
            if (emitter != null) {
                emitter.send(SseEmitter.event().name(event).data(message));
            } else {
                log.warn("Emitter for client {} is disposed or null, skipping notification", clientId);
            }
        } catch (IOException e) {
            log.error("Error sending notification to client: {}", clientId, e);
            emittersMap.remove(clientId); // 从对应的Map中移除
        } catch (Exception e) {
            log.error("Unexpected error sending notification to client: {}", clientId, e);
            emittersMap.remove(clientId);
        }
    }

}