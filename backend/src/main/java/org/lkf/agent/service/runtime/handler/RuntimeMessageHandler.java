package org.lkf.agent.service.runtime.handler;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface RuntimeMessageHandler {

    String key();

    void handle(String username, String conversationId, SseEmitter emitter) throws IOException;
}
