package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Agent响应对象")
public class AgentResponseObject {

    @Schema(description = "Agent ID")
    private String id;

    @Schema(description = "Agent名称")
    private String name;

    @Schema(description = "Agent描述")
    private String description;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "头像地址")
    private String avatarUrl;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "绑定工具ID列表")
    private List<String> toolIds;

    @Schema(description = "绑定知识库ID列表")
    private List<String> knowledgeBaseIds;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public AgentResponseObject() {
    }

    public AgentResponseObject(String id, String name, String description, String systemPrompt, String avatarUrl, String status,
                               List<String> toolIds, List<String> knowledgeBaseIds, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.avatarUrl = avatarUrl;
        this.status = status;
        this.toolIds = toolIds;
        this.knowledgeBaseIds = knowledgeBaseIds;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
