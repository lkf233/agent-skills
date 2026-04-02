package org.lkf.agent.service.runtime.handler;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RuntimeMessageHandlerFactory {

    private final List<RuntimeMessageHandler> handlers;

    public RuntimeMessageHandlerFactory(List<RuntimeMessageHandler> handlers) {
        this.handlers = handlers;
    }

    public RuntimeMessageHandler getHandler(String key) {
        if (handlers == null || handlers.isEmpty()) {
            throw new IllegalStateException("未注册RuntimeMessageHandler");
        }
        String normalized = key == null ? "" : key.trim().toLowerCase();
        for (RuntimeMessageHandler handler : handlers) {
            if (handler.key().equalsIgnoreCase(normalized)) {
                return handler;
            }
        }
        return handlers.get(0);
    }
}
