package org.lkf.agent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.lkf.agent.service.McpToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private static final Logger logger = LoggerFactory.getLogger(McpController.class);
    private final McpToolService mcpToolService;

    public McpController(McpToolService mcpToolService) {
        this.mcpToolService = mcpToolService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handle(@RequestBody JsonNode request, HttpServletRequest httpServletRequest) {
        long startedAt = System.currentTimeMillis();
        String traceId = resolveTraceId(httpServletRequest);
        String method = readMethod(request);
        String requestId = readRequestId(request);
        logger.info(
                "mcp request traceId={} method={} requestId={} remoteAddr={} userAgent={} sessionId={} body={}",
                traceId,
                method,
                requestId,
                safeText(httpServletRequest.getRemoteAddr()),
                safeText(httpServletRequest.getHeader("User-Agent")),
                safeText(httpServletRequest.getHeader("mcp-session-id")),
                compactJson(request)
        );
        if (request == null || !request.isObject()) {
            return complete(traceId, method, startedAt, ResponseEntity.badRequest().body(errorResponse(null, -32600, "Invalid Request")),
                    "invalid_request_body");
        }
        if (!"2.0".equals(request.path("jsonrpc").asText(null))) {
            return complete(traceId, method, startedAt, ResponseEntity.badRequest().body(errorResponse(null, -32600, "Invalid Request")),
                    "invalid_jsonrpc_version");
        }
        JsonNode idNode = request.get("id");
        boolean notification = idNode == null || idNode.isNull();
        Object requestId = readId(idNode);
        String method = request.path("method").asText(null);
        JsonNode params = request.get("params");
        if (method == null || method.isBlank()) {
            ResponseEntity<?> response = notification ? ResponseEntity.noContent().build()
                    : ResponseEntity.badRequest().body(errorResponse(requestId, -32600, "Invalid Request"));
            return complete(traceId, method, startedAt, response, "invalid_method");
        }
        if ("notifications/initialized".equals(method)) {
            return complete(traceId, method, startedAt, ResponseEntity.noContent().build(), "initialized_notification");
        }
        if ("initialize".equals(method)) {
            Map<String, Object> result = Map.of(
                    "protocolVersion", "2024-11-05",
                    "capabilities", Map.of("tools", Map.of("listChanged", false)),
                    "serverInfo", Map.of("name", "agent-chat-mcp-server", "version", "1.0.0")
            );
            ResponseEntity<?> response = notification ? ResponseEntity.noContent().build() : ResponseEntity.ok(resultResponse(requestId, result));
            return complete(traceId, method, startedAt, response, "initialize");
        }
        if ("ping".equals(method)) {
            ResponseEntity<?> response = notification ? ResponseEntity.noContent().build() : ResponseEntity.ok(resultResponse(requestId, Map.of()));
            return complete(traceId, method, startedAt, response, "ping");
        }
        if ("tools/list".equals(method)) {
            Map<String, Object> result = Map.of("tools", mcpToolService.listTools());
            int toolCount = mcpToolService.listTools().size();
            ResponseEntity<?> response = notification ? ResponseEntity.noContent().build() : ResponseEntity.ok(resultResponse(requestId, result));
            return complete(traceId, method, startedAt, response, "tools_list_count_" + toolCount);
        }
        if ("tools/call".equals(method)) {
            if (params == null || !params.isObject()) {
                ResponseEntity<?> response = notification ? ResponseEntity.noContent().build()
                        : ResponseEntity.ok(errorResponse(requestId, -32602, "Invalid params"));
                return complete(traceId, method, startedAt, response, "tools_call_invalid_params");
            }
            String toolName = params.path("name").asText(null);
            if (toolName == null || toolName.isBlank()) {
                ResponseEntity<?> response = notification ? ResponseEntity.noContent().build()
                        : ResponseEntity.ok(errorResponse(requestId, -32602, "Invalid params"));
                return complete(traceId, method, startedAt, response, "tools_call_missing_tool_name");
            }
            JsonNode arguments = params.get("arguments");
            logger.info("mcp tools/call traceId={} toolName={} arguments={}", traceId, toolName, compactJson(arguments));
            Map<String, Object> result = mcpToolService.callTool(toolName, arguments);
            Object isError = result.get("isError");
            ResponseEntity<?> response = notification ? ResponseEntity.noContent().build() : ResponseEntity.ok(resultResponse(requestId, result));
            return complete(traceId, method, startedAt, response, "tools_call_" + toolName + "_isError_" + String.valueOf(isError));
        }
        ResponseEntity<?> response = notification ? ResponseEntity.noContent().build() : ResponseEntity.ok(errorResponse(requestId, -32601, "Method not found"));
        return complete(traceId, method, startedAt, response, "method_not_found");
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

    private ResponseEntity<?> complete(String traceId, String method, long startedAt, ResponseEntity<?> response, String outcome) {
        long costMs = System.currentTimeMillis() - startedAt;
        logger.info("mcp response traceId={} method={} status={} costMs={} outcome={}",
                traceId, method, response.getStatusCode().value(), costMs, outcome);
        return response;
    }

    private String resolveTraceId(HttpServletRequest request) {
        String requestId = request.getHeader("x-request-id");
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return requestId.trim();
    }

    private String readMethod(JsonNode request) {
        if (request == null || !request.isObject()) {
            return "UNKNOWN";
        }
        String method = request.path("method").asText(null);
        if (method == null || method.isBlank()) {
            return "UNKNOWN";
        }
        return method;
    }

    private String readRequestId(JsonNode request) {
        if (request == null || !request.isObject()) {
            return "";
        }
        JsonNode idNode = request.get("id");
        Object id = readId(idNode);
        return id == null ? "" : String.valueOf(id);
    }

    private String compactJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        String value = node.toString();
        if (value.length() <= 1000) {
            return value;
        }
        return value.substring(0, 1000);
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.length() <= 200) {
            return value;
        }
        return value.substring(0, 200);
    }
}
