package org.lkf.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lkf.agent.common.exception.BusinessException;
import org.lkf.agent.dto.CreateToolRequestObject;
import org.lkf.agent.dto.ToolResponseObject;
import org.lkf.agent.dto.ToolTestRequestObject;
import org.lkf.agent.dto.ToolTestResponseObject;
import org.lkf.agent.dto.UpdateToolRequestObject;
import org.lkf.agent.entity.ToolDefEntity;
import org.lkf.agent.entity.UserAccountEntity;
import org.lkf.agent.mapper.ToolDefMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ToolAppService {

    private final ToolDefMapper toolDefMapper;
    private final AuthAppService authAppService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ToolAppService(ToolDefMapper toolDefMapper, AuthAppService authAppService, ObjectMapper objectMapper) {
        this.toolDefMapper = toolDefMapper;
        this.authAppService = authAppService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public ToolResponseObject createTool(String username, CreateToolRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        validateToolType(requestObject.getToolType());
        validateMcpConfig(requestObject.getConfigJson());
        ToolDefEntity entity = new ToolDefEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setUserId(userAccountEntity.getId());
        entity.setName(requestObject.getName().trim());
        entity.setDescription(requestObject.getDescription() == null ? "" : requestObject.getDescription().trim());
        entity.setToolType(requestObject.getToolType().trim().toUpperCase(Locale.ROOT));
        entity.setStatus(normalizeStatus(requestObject.getStatus(), "DRAFT"));
        entity.setConfigJson(writeJson(requestObject.getConfigJson()));
        entity.setAuthConfigJson(writeJson(requestObject.getAuthConfigJson() == null ? objectMapper.createObjectNode() : requestObject.getAuthConfigJson()));
        int updated = toolDefMapper.insert(entity);
        if (updated <= 0) {
            throw new BusinessException("创建工具失败");
        }
        ToolDefEntity created = toolDefMapper.findByIdAndUserId(entity.getId(), userAccountEntity.getId());
        return toResponseObject(created);
    }

    public List<ToolResponseObject> listTools(String username, String keyword, String toolType, String status) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedToolType = toolType == null || toolType.isBlank() ? null : toolType.trim().toUpperCase(Locale.ROOT);
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
        List<ToolDefEntity> entities = toolDefMapper.listByUserId(userAccountEntity.getId(), normalizedKeyword, normalizedToolType, normalizedStatus);
        List<ToolResponseObject> responseObjectList = new ArrayList<>();
        for (ToolDefEntity entity : entities) {
            responseObjectList.add(toResponseObject(entity));
        }
        return responseObjectList;
    }

    public ToolResponseObject getTool(String username, String toolId) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        ToolDefEntity entity = mustFindTool(toolId, userAccountEntity.getId());
        return toResponseObject(entity);
    }

    public void updateTool(String username, String toolId, UpdateToolRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        ToolDefEntity existing = mustFindTool(toolId, userAccountEntity.getId());
        if (requestObject.getName() != null && !requestObject.getName().isBlank()) {
            existing.setName(requestObject.getName().trim());
        }
        if (requestObject.getDescription() != null) {
            existing.setDescription(requestObject.getDescription().trim());
        }
        if (requestObject.getStatus() != null) {
            existing.setStatus(normalizeStatus(requestObject.getStatus(), existing.getStatus()));
        }
        if (requestObject.getConfigJson() != null) {
            validateMcpConfig(requestObject.getConfigJson());
            existing.setConfigJson(writeJson(requestObject.getConfigJson()));
        }
        if (requestObject.getAuthConfigJson() != null) {
            existing.setAuthConfigJson(writeJson(requestObject.getAuthConfigJson()));
        }
        int updated = toolDefMapper.update(existing);
        if (updated <= 0) {
            throw new BusinessException("更新工具失败");
        }
    }

    public void deleteTool(String username, String toolId) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        int updated = toolDefMapper.softDelete(toolId, userAccountEntity.getId());
        if (updated <= 0) {
            throw new BusinessException("工具不存在");
        }
    }

    public ToolTestResponseObject testTool(String username, String toolId, ToolTestRequestObject requestObject) {
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        ToolDefEntity entity = mustFindTool(toolId, userAccountEntity.getId());
        JsonNode configNode = readJson(entity.getConfigJson());
        String endpoint = readEndpoint(configNode);
        JsonNode testInput = requestObject == null || requestObject.getInputJson() == null
                ? defaultMcpTestInput()
                : requestObject.getInputJson();
        JsonNode responseNode = invokeRemoteMcp(endpoint, readJson(entity.getAuthConfigJson()), testInput);
        boolean connected = responseNode.has("result") && !responseNode.has("error");
        String message = connected ? "远程MCP工具连通成功" : "远程MCP工具连通失败";
        return new ToolTestResponseObject(connected, message, responseNode);
    }

    private ToolDefEntity mustFindTool(String toolId, Long userId) {
        ToolDefEntity entity = toolDefMapper.findByIdAndUserId(toolId, userId);
        if (entity == null) {
            throw new BusinessException("工具不存在");
        }
        return entity;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private String normalizeStatus(String status, String defaultValue) {
        if (status == null || status.isBlank()) {
            return defaultValue;
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private void validateToolType(String toolType) {
        if (toolType == null || toolType.isBlank()) {
            throw new BusinessException("toolType不能为空");
        }
        String normalized = toolType.trim().toUpperCase(Locale.ROOT);
        if (!"REMOTE_MCP".equals(normalized)) {
            throw new BusinessException("当前仅支持REMOTE_MCP类型工具");
        }
    }

    private void validateMcpConfig(JsonNode configJson) {
        if (configJson == null || !configJson.isObject()) {
            throw new BusinessException("configJson必须为对象");
        }
        String endpoint = readEndpoint(configJson);
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            throw new BusinessException("configJson.endpoint必须以http://或https://开头");
        }
    }

    private String readEndpoint(JsonNode configJson) {
        JsonNode endpointNode = configJson.get("endpoint");
        if (endpointNode == null || endpointNode.isNull() || !endpointNode.isTextual() || endpointNode.asText().isBlank()) {
            throw new BusinessException("configJson.endpoint不能为空");
        }
        return endpointNode.asText().trim();
    }

    private String writeJson(JsonNode jsonNode) {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception exception) {
            throw new BusinessException("JSON序列化失败");
        }
    }

    private JsonNode readJson(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (IOException exception) {
            throw new BusinessException("JSON解析失败");
        }
    }

    private ToolResponseObject toResponseObject(ToolDefEntity entity) {
        return new ToolResponseObject(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getToolType(),
                entity.getStatus(),
                readJson(entity.getConfigJson()),
                readJson(entity.getAuthConfigJson()),
                entity.getUpdatedAt()
        );
    }

    private JsonNode defaultMcpTestInput() {
        return readJson("""
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "method": "tools/list",
                  "params": {}
                }
                """);
    }

    private JsonNode invokeRemoteMcp(String endpoint, JsonNode authConfig, JsonNode payload) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(writeJson(payload)));
            appendAuthHeader(requestBuilder, authConfig);
            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("远程MCP响应异常，状态码：" + response.statusCode());
            }
            return readJson(response.body());
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("远程MCP连通失败: " + exception.getMessage());
        }
    }

    private void appendAuthHeader(HttpRequest.Builder requestBuilder, JsonNode authConfig) {
        if (authConfig == null || !authConfig.isObject()) {
            return;
        }
        JsonNode bearerTokenNode = authConfig.get("bearerToken");
        if (bearerTokenNode != null && bearerTokenNode.isTextual() && !bearerTokenNode.asText().isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + bearerTokenNode.asText().trim());
        }
        JsonNode apiKeyHeaderNameNode = authConfig.get("apiKeyHeaderName");
        JsonNode apiKeyNode = authConfig.get("apiKey");
        if (apiKeyHeaderNameNode != null && apiKeyNode != null && apiKeyHeaderNameNode.isTextual() && apiKeyNode.isTextual()
                && !apiKeyHeaderNameNode.asText().isBlank() && !apiKeyNode.asText().isBlank()) {
            requestBuilder.header(apiKeyHeaderNameNode.asText().trim(), apiKeyNode.asText().trim());
        }
    }
}
