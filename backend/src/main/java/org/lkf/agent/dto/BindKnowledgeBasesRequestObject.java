package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "绑定知识库请求对象")
public class BindKnowledgeBasesRequestObject {

    @Schema(description = "知识库ID列表")
    private List<String> knowledgeBaseIds;

    public List<String> getKnowledgeBaseIds() {
        return knowledgeBaseIds;
    }

    public void setKnowledgeBaseIds(List<String> knowledgeBaseIds) {
        this.knowledgeBaseIds = knowledgeBaseIds;
    }
}
