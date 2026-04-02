package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "绑定工具请求对象")
public class BindToolsRequestObject {

    @Schema(description = "工具ID列表")
    private List<String> toolIds;

    public List<String> getToolIds() {
        return toolIds;
    }

    public void setToolIds(List<String> toolIds) {
        this.toolIds = toolIds;
    }
}
