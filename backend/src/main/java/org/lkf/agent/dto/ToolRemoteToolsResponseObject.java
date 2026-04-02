package org.lkf.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "远程MCP工具列表响应对象")
public class ToolRemoteToolsResponseObject {

    @Schema(description = "是否为有效MCP工具服务")
    private Boolean validMcpService;

    @Schema(description = "结果消息")
    private String message;

    @Schema(description = "远程MCP工具列表")
    private JsonNode toolsJson;

    @Schema(description = "远端原始返回")
    private JsonNode rawResponseJson;

    public ToolRemoteToolsResponseObject() {
    }

    public ToolRemoteToolsResponseObject(Boolean validMcpService, String message, JsonNode toolsJson, JsonNode rawResponseJson) {
        this.validMcpService = validMcpService;
        this.message = message;
        this.toolsJson = toolsJson;
        this.rawResponseJson = rawResponseJson;
    }

    public Boolean getValidMcpService() {
        return validMcpService;
    }

    public void setValidMcpService(Boolean validMcpService) {
        this.validMcpService = validMcpService;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonNode getToolsJson() {
        return toolsJson;
    }

    public void setToolsJson(JsonNode toolsJson) {
        this.toolsJson = toolsJson;
    }

    public JsonNode getRawResponseJson() {
        return rawResponseJson;
    }

    public void setRawResponseJson(JsonNode rawResponseJson) {
        this.rawResponseJson = rawResponseJson;
    }
}
