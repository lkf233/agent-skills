package org.lkf.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "工具响应对象")
public class ToolResponseObject {

    @Schema(description = "工具ID")
    private String id;

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "工具类型")
    private String toolType;

    @Schema(description = "工具状态")
    private String status;

    @Schema(description = "工具配置")
    private JsonNode configJson;

    @Schema(description = "工具鉴权配置")
    private JsonNode authConfigJson;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public ToolResponseObject() {
    }

    public ToolResponseObject(String id, String name, String description, String toolType, String status, JsonNode configJson,
                              JsonNode authConfigJson, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.toolType = toolType;
        this.status = status;
        this.configJson = configJson;
        this.authConfigJson = authConfigJson;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToolType() {
        return toolType;
    }

    public void setToolType(String toolType) {
        this.toolType = toolType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonNode getConfigJson() {
        return configJson;
    }

    public void setConfigJson(JsonNode configJson) {
        this.configJson = configJson;
    }

    public JsonNode getAuthConfigJson() {
        return authConfigJson;
    }

    public void setAuthConfigJson(JsonNode authConfigJson) {
        this.authConfigJson = authConfigJson;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
