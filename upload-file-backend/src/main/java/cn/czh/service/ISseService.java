package cn.czh.service;

import cn.czh.dto.NotifyMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ISseService {

    /**
     * 通用订阅
     */
    SseEmitter subscribe(String remoteAddr);

    /**
     * 通用通知
     */
    void notification(NotifyMessage message);

}
