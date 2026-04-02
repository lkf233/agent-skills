package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "更新Agent请求对象")
public class UpdateAgentRequestObject {

    @Schema(description = "Agent名称")
    private String name;

    @Schema(description = "Agent描述")
    private String description;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "头像地址")
    private String avatarUrl;

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

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
