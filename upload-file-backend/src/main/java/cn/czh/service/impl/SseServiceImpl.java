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

    private final Map<String, SseEmitter> clientEmitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(String remoteAddr) {
        SseEmitter emitter = new SseEmitter(0L);
        clientEmitters.put(remoteAddr, emitter);

        emitter.onCompletion(() -> clientEmitters.remove(remoteAddr));
        emitter.onTimeout(() -> clientEmitters.remove(remoteAddr));
        return emitter;
    }

    @Async
    @Override
    public void notification(NotifyMessage message) {
        clientEmitters.forEach((clientId, emitter) ->
                sendNotification(clientId, emitter, message.getType(), JSONUtil.toJsonStr(message), clientEmitters));
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