package cn.czh.service;

import cn.czh.dto.NotifyMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ISseService {

    /**
     * 订阅共享文件更新
     */
    SseEmitter subscribeSharedFiles(String clientId);

    /**
     * 订阅系统事件
     */
    SseEmitter subscribeSystemEvent(String clientId);

    /**
     * 通知共享文件更新
     */
    void notifySharedFileUpdate(String message);

    /**
     * 通知系统事件
     */
    void notifySystemEvent(NotifyMessage message);

}
