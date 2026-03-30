package org.lkf.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "工具连通性测试响应对象")
public class ToolTestResponseObject {

    @Schema(description = "是否连通")
    private Boolean connected;

    @Schema(description = "测试结果消息")
    private String message;

    @Schema(description = "远端返回结果")
    private JsonNode responseJson;

    public ToolTestResponseObject() {
    }

    public ToolTestResponseObject(Boolean connected, String message, JsonNode responseJson) {
        this.connected = connected;
        this.message = message;
        this.responseJson = responseJson;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonNode getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(JsonNode responseJson) {
        this.responseJson = responseJson;
    }
}
