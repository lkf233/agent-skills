package org.lkf.agent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.lkf.agent.service.McpToolService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private final McpToolService mcpToolService;

    public McpController(McpToolService mcpToolService) {
        this.mcpToolService = mcpToolService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handle(@RequestBody JsonNode request) {
        if (request == null || !request.isObject()) {
            return ResponseEntity.badRequest().body(errorResponse(null, -32600, "Invalid Request"));
        }
        if (!"2.0".equals(request.path("jsonrpc").asText(null))) {
            return ResponseEntity.badRequest().body(errorResponse(null, -32600, "Invalid Request"));
        }
        JsonNode idNode = request.get("id");
        boolean notification = idNode == null || idNode.isNull();
        Object requestId = readId(idNode);
        String method = request.path("method").asText(null);
        JsonNode params = request.get("params");
        if (method == null || method.isBlank()) {
            return notification ? ResponseEntity.noContent().build()
                    : ResponseEntity.badRequest().body(errorResponse(requestId, -32600, "Invalid Request"));
        }
        if ("notifications/initialized".equals(method)) {
            return ResponseEntity.noContent().build();
        }
        if ("initialize".equals(method)) {
            Map<String, Object> result = Map.of(
                    "protocolVersion", "2024-11-05",
                    "capabilities", Map.of("tools", Map.of("listChanged", false)),
                    "serverInfo", Map.of("name", "agent-chat-mcp-server", "version", "1.0.0")
            );
            return notification ? ResponseEntity.noContent().build() : ResponseEntity.ok(resultResponse(requestId, result));
        }
        if ("ping".equals(method)) {
            return notification ? ResponseEntity.noContent().build() : ResponseEntity.ok(resultResponse(requestId, Map.of()));
        }
        if ("tools/list".equals(method)) {
            Map<String, Object> result = Map.of("tools", mcpToolService.listTools());
            return notification ? ResponseEntity.noContent().build() : ResponseEntity.ok(resultResponse(requestId, result));
        }
        if ("tools/call".equals(method)) {
            if (params == null || !params.isObject()) {
                return notification ? ResponseEntity.noContent().build()
                        : ResponseEntity.ok(errorResponse(requestId, -32602, "Invalid params"));
            }
            String toolName = params.path("name").asText(null);
            if (toolName == null || toolName.isBlank()) {
                return notification ? ResponseEntity.noContent().build()
                        : ResponseEntity.ok(errorResponse(requestId, -32602, "Invalid params"));
            }
            JsonNode arguments = params.get("arguments");
            Map<String, Object> result = mcpToolService.callTool(toolName, arguments);
            return notification ? ResponseEntity.noContent().build() : ResponseEntity.ok(resultResponse(requestId, result));
        }
        return notification ? ResponseEntity.noContent().build() : ResponseEntity.ok(errorResponse(requestId, -32601, "Method not found"));
    }

    private Map<String, Object> resultResponse(Object id, Object result) {
        return Map.of("jsonrpc", "2.0", "id", id, "result", result);
    }

    private Map<String, Object> errorResponse(Object id, int code, String message) {
        return Map.of(
                "jsonrpc", "2.0",
                "id", id,
                "error", Map.of("code", code, "message", message)
        );
    }

    private Object readId(JsonNode idNode) {
        if (idNode == null || idNode.isNull()) {
            return null;
        }
        if (idNode.isTextual()) {
            return idNode.asText();
        }
        if (idNode.isIntegralNumber()) {
            return idNode.asLong();
        }
        if (idNode.isFloatingPointNumber()) {
            return idNode.asDouble();
        }
        return idNode.toString();
    }
}
