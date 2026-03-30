package org.lkf.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
public class McpToolService {

    private final ObjectMapper objectMapper;

    public McpToolService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> listTools() {
        return List.of(
                Map.of(
                        "name", "echo_text",
                        "description", "原样返回输入文本",
                        "inputSchema", toMap("""
                                {
                                  "type": "object",
                                  "properties": {
                                    "text": {
                                      "type": "string",
                                      "description": "需要回显的文本"
                                    }
                                  },
                                  "required": ["text"],
                                  "additionalProperties": false
                                }
                                """)
                ),
                Map.of(
                        "name", "add_numbers",
                        "description", "计算两个数字之和",
                        "inputSchema", toMap("""
                                {
                                  "type": "object",
                                  "properties": {
                                    "a": {
                                      "type": "number",
                                      "description": "第一个数字"
                                    },
                                    "b": {
                                      "type": "number",
                                      "description": "第二个数字"
                                    }
                                  },
                                  "required": ["a", "b"],
                                  "additionalProperties": false
                                }
                                """)
                ),
                Map.of(
                        "name", "get_server_time",
                        "description", "获取当前服务端UTC时间",
                        "inputSchema", toMap("""
                                {
                                  "type": "object",
                                  "properties": {},
                                  "additionalProperties": false
                                }
                                """)
                )
        );
    }

    public Map<String, Object> callTool(String toolName, JsonNode arguments) {
        if ("echo_text".equals(toolName)) {
            JsonNode textNode = arguments == null ? null : arguments.get("text");
            if (textNode == null || textNode.isNull() || !textNode.isTextual()) {
                return toolError("参数 text 必须为字符串");
            }
            return toolOk(textNode.asText());
        }
        if ("add_numbers".equals(toolName)) {
            JsonNode aNode = arguments == null ? null : arguments.get("a");
            JsonNode bNode = arguments == null ? null : arguments.get("b");
            if (aNode == null || bNode == null || !aNode.isNumber() || !bNode.isNumber()) {
                return toolError("参数 a 和 b 必须为数字");
            }
            double sum = aNode.asDouble() + bNode.asDouble();
            return toolOk("计算结果: " + sum);
        }
        if ("get_server_time".equals(toolName)) {
            return toolOk(OffsetDateTime.now(ZoneOffset.UTC).toString());
        }
        return toolError("未知工具: " + toolName);
    }

    private Map<String, Object> toolOk(String text) {
        return Map.of(
                "content", List.of(Map.of("type", "text", "text", text)),
                "isError", false
        );
    }

    private Map<String, Object> toolError(String text) {
        return Map.of(
                "content", List.of(Map.of("type", "text", "text", text)),
                "isError", true
        );
    }

    private Map<String, Object> toMap(String json) {
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            return objectMapper.convertValue(jsonNode, Map.class);
        } catch (Exception exception) {
            throw new IllegalStateException("构建工具Schema失败", exception);
        }
    }
}
