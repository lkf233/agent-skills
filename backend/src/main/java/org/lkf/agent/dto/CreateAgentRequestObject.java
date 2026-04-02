package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "创建Agent请求对象")
public class CreateAgentRequestObject {

    @NotBlank(message = "Agent名称不能为空")
    @Schema(description = "Agent名称")
    private String name;

    @Schema(description = "Agent描述")
    private String description;

    @NotBlank(message = "系统提示词不能为空")
    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "头像地址")
    private String avatarUrl;

    @Schema(description = "初始化绑定工具ID列表")
    private List<String> toolIds;

    @Schema(description = "初始化绑定知识库ID列表")
    private List<String> knowledgeBaseIds;

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

    public List<String> getToolIds() {
        return toolIds;
    }

    public void setToolIds(List<String> toolIds) {
        this.toolIds = toolIds;
    }

    public List<String> getKnowledgeBaseIds() {
        return knowledgeBaseIds;
    }

    public void setKnowledgeBaseIds(List<String> knowledgeBaseIds) {
        this.knowledgeBaseIds = knowledgeBaseIds;
    }
}
