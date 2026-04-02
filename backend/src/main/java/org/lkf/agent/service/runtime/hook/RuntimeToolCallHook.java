package org.lkf.agent.service.runtime.hook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lkf.agent.common.exception.BusinessException;
import org.lkf.agent.entity.ToolCallLogEntity;
import org.lkf.agent.mapper.ToolCallLogMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RuntimeToolCallHook {

    private final ToolCallLogMapper toolCallLogMapper;
    private final ObjectMapper objectMapper;

    public RuntimeToolCallHook(ToolCallLogMapper toolCallLogMapper, ObjectMapper objectMapper) {
        this.toolCallLogMapper = toolCallLogMapper;
        this.objectMapper = objectMapper;
    }

    public void onToolCallCompleted(String conversationId, String toolDefId, String toolName, JsonNode request, JsonNode response,
                                    boolean success, long latencyMs) {
        ToolCallLogEntity logEntity = new ToolCallLogEntity();
        logEntity.setId(UUID.randomUUID().toString().replace("-", ""));
        logEntity.setConversationId(conversationId);
        logEntity.setMessageId("");
        logEntity.setToolDefId(toolDefId == null ? "" : toolDefId);
        logEntity.setToolName(toolName == null ? "" : toolName);
        logEntity.setRequestJson(writeJson(request == null ? objectMapper.createObjectNode() : request));
        logEntity.setResponseJson(writeJson(response == null ? objectMapper.createObjectNode() : response));
        logEntity.setStatus(success ? "SUCCESS" : "FAILED");
        logEntity.setLatencyMs(latencyMs);
        toolCallLogMapper.insert(logEntity);
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception exception) {
            throw new BusinessException("JSON序列化失败");
        }
    }
}
