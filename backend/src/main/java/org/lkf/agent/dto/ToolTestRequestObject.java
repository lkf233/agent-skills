package org.lkf.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "工具连通性测试请求对象")
public class ToolTestRequestObject {

    @Schema(description = "测试输入")
    private JsonNode inputJson;

    public JsonNode getInputJson() {
        return inputJson;
    }

    public void setInputJson(JsonNode inputJson) {
        this.inputJson = inputJson;
    }
}
