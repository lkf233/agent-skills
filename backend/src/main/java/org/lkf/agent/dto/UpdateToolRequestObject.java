package org.lkf.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "更新工具请求对象")
public class UpdateToolRequestObject {

    @Schema(description = "工具名称", example = "企业CRM远程MCP")
    private String name;

    @Schema(description = "工具描述", example = "用于查询CRM客户信息")
    private String description;

    @Schema(description = "工具状态", example = "ACTIVE")
    private String status;

    @Schema(description = "工具配置")
    private JsonNode configJson;

    @Schema(description = "工具鉴权配置")
    private JsonNode authConfigJson;

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
}
