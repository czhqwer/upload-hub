package cn.czh.controller;

import cn.czh.service.ISseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/sse")
public class SseController {

    @Resource
    private ISseService sseService;

    @GetMapping("/subscribeSharedFiles")
    public SseEmitter subscribeSharedFiles(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return sseService.subscribeSharedFiles(remoteAddr);
    }

    @GetMapping("/subscribeSystemEvent")
    public SseEmitter subscribeSystemEvent(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return sseService.subscribeSystemEvent(remoteAddr);
    }


}
