package org.lkf.agent.service.runtime.tool;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.service.tool.ToolProvider;
import org.lkf.agent.dto.ToolResponseObject;
import org.lkf.agent.service.ToolAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class RuntimeMcpAgentToolManager {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeMcpAgentToolManager.class);
    private final ToolAppService toolAppService;

    public RuntimeMcpAgentToolManager(ToolAppService toolAppService) {
        this.toolAppService = toolAppService;
    }

    public ToolProvider createToolProvider(String username, List<String> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return null;
        }
        List<McpClient> mcpClients = new ArrayList<>();
        for (String toolId : toolIds) {
            if (toolId == null || toolId.isBlank()) {
                continue;
            }
            try {
                ToolResponseObject tool = toolAppService.getTool(username, toolId.trim());
                JsonNode configJson = tool == null ? null : tool.getConfigJson();
                String endpoint = readEndpoint(configJson);
                if (endpoint.isBlank()) {
                    continue;
                }
                String transportMode = readTransportMode(configJson);
                HttpMcpTransport.Builder builder = new HttpMcpTransport.Builder()
                        .logRequests(true)
                        .logResponses(true)
                        .timeout(Duration.ofMinutes(5));
                configureTransportEndpoint(builder, endpoint, transportMode);
                applyAuthHeaders(builder, resolveAuthHeaders(tool == null ? null : tool.getAuthConfigJson()));
                McpTransport transport = builder.build();
                McpClient mcpClient = new DefaultMcpClient.Builder()
                        .transport(transport)
                        .build();
                mcpClients.add(mcpClient);
            } catch (Exception exception) {
                logger.warn("构建MCP客户端失败，已跳过该工具。toolId={}, reason={}", toolId, exception.toString());
            }
        }
        if (mcpClients.isEmpty()) {
            return null;
        }
        return McpToolProvider.builder().mcpClients(mcpClients).build();
    }

    private String readEndpoint(JsonNode configJson) {
        if (configJson == null || configJson.get("endpoint") == null || configJson.get("endpoint").asText().isBlank()) {
            return "";
        }
        return configJson.get("endpoint").asText().trim();
    }

    private String readTransportMode(JsonNode configJson) {
        if (configJson == null || configJson.get("transportMode") == null || configJson.get("transportMode").asText().isBlank()) {
            return "AUTO";
        }
        return configJson.get("transportMode").asText().trim().toUpperCase();
    }

    private void configureTransportEndpoint(HttpMcpTransport.Builder builder, String endpoint, String transportMode) {
        if ("SSE".equals(transportMode)) {
            builder.sseUrl(endpoint);
            return;
        }
        if ("POST".equals(transportMode)) {
            if (!configurePostEndpoint(builder, endpoint)) {
                throw new IllegalStateException("当前MCP客户端版本不支持POST传输模式");
            }
            return;
        }
        if (endpoint.contains("/mcp")) {
            if (configurePostEndpoint(builder, endpoint)) {
                logger.info("MCP运行时传输模式=POST endpoint={}", endpoint);
                return;
            }
            logger.warn("MCP运行时尝试POST模式失败，回退SSE endpoint={}", endpoint);
            builder.sseUrl(endpoint);
            return;
        }
        builder.sseUrl(endpoint);
    }

    private boolean configurePostEndpoint(HttpMcpTransport.Builder builder, String endpoint) {
        return invokeStringMethod(builder, "streamableHttpUrl", endpoint)
                || invokeStringMethod(builder, "httpUrl", endpoint)
                || invokeStringMethod(builder, "url", endpoint);
    }

    private Map<String, String> resolveAuthHeaders(JsonNode authConfigJson) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (authConfigJson == null || authConfigJson.isNull()) {
            return headers;
        }
        if (authConfigJson.get("bearerToken") != null && !authConfigJson.get("bearerToken").asText().isBlank()) {
            headers.put("Authorization", "Bearer " + authConfigJson.get("bearerToken").asText().trim());
        }
        JsonNode customHeaders = authConfigJson.get("customHeaders");
        if (customHeaders != null && customHeaders.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = customHeaders.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null && !entry.getValue().asText().isBlank()) {
                    headers.put(entry.getKey().trim(), entry.getValue().asText().trim());
                }
            }
        }
        return headers;
    }

    private void applyAuthHeaders(HttpMcpTransport.Builder builder, Map<String, String> headers) {
        if (builder == null || headers == null || headers.isEmpty()) {
            return;
        }
        if (invokeMapMethod(builder, "headers", headers)
                || invokeMapMethod(builder, "requestHeaders", headers)
                || invokeMapMethod(builder, "defaultHeaders", headers)
                || invokeMapMethod(builder, "customHeaders", headers)) {
            return;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (invokePairMethod(builder, "header", entry.getKey(), entry.getValue())
                    || invokePairMethod(builder, "addHeader", entry.getKey(), entry.getValue())
                    || invokePairMethod(builder, "requestHeader", entry.getKey(), entry.getValue())
                    || invokePairMethod(builder, "defaultHeader", entry.getKey(), entry.getValue())
                    || invokePairMethod(builder, "customHeader", entry.getKey(), entry.getValue())) {
                continue;
            }
        }
    }

    private boolean invokeMapMethod(Object target, String methodName, Map<String, String> headers) {
        try {
            Method method = target.getClass().getMethod(methodName, Map.class);
            method.invoke(target, headers);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean invokePairMethod(Object target, String methodName, String key, String value) {
        try {
            Method method = target.getClass().getMethod(methodName, String.class, String.class);
            method.invoke(target, key, value);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean invokeStringMethod(Object target, String methodName, String value) {
        try {
            Method method = target.getClass().getMethod(methodName, String.class);
            method.invoke(target, value);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
