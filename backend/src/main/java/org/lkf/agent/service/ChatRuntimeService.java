package org.lkf.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.lkf.agent.dto.SendMessageRequestObject;
import org.lkf.agent.service.runtime.handler.RuntimeMessageHandlerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Service
public class ChatRuntimeService {

    private final ObjectMapper objectMapper;
    private final RuntimeMessageHandlerFactory runtimeMessageHandlerFactory;
    private final ConversationAppService conversationAppService;

    public ChatRuntimeService(ObjectMapper objectMapper,
                              RuntimeMessageHandlerFactory runtimeMessageHandlerFactory,
                              ConversationAppService conversationAppService) {
        this.objectMapper = objectMapper;
        this.runtimeMessageHandlerFactory = runtimeMessageHandlerFactory;
        this.conversationAppService = conversationAppService;
    }

    public SseEmitter streamConversation(String username, String conversationId, String content) {
        SendMessageRequestObject requestObject = new SendMessageRequestObject();
        requestObject.setContent(content);
        conversationAppService.sendMessage(username, conversationId, requestObject);
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> executeStreaming(username, conversationId, emitter));
        return emitter;
    }

    private void executeStreaming(String username, String conversationId, SseEmitter emitter) {
        try {
            runtimeMessageHandlerFactory.getHandler("agent").handle(username, conversationId, emitter);
        } catch (Exception exception) {
            System.out.println(Arrays.toString(exception.getStackTrace()));
            System.out.println(exception.getMessage());
            System.out.println(exception.toString());
            try {
                emitter.send(SseEmitter.event().name("error")
                        .data(objectMapper.createObjectNode().put("message",
                                exception.getMessage() == null ? "运行异常" : exception.getMessage())));
            } catch (Exception ignored) {
            }
            emitter.complete();
        }
    }
}
