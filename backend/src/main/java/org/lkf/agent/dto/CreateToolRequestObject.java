package org.lkf.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "创建工具请求对象")
public class CreateToolRequestObject {

    @NotBlank(message = "工具名称不能为空")
    @Schema(description = "工具名称", example = "企业CRM远程MCP")
    private String name;

    @Schema(description = "工具描述", example = "用于查询CRM客户信息")
    private String description;

    @NotBlank(message = "工具类型不能为空")
    @Schema(description = "工具类型", example = "REMOTE_MCP")
    private String toolType;

    @NotNull(message = "工具配置不能为空")
    @Schema(description = "工具配置")
    private JsonNode configJson;

    @Schema(description = "工具鉴权配置")
    private JsonNode authConfigJson;

    @Schema(description = "工具状态", example = "DRAFT")
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
