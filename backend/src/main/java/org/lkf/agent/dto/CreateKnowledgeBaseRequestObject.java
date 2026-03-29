package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 创建知识库请求对象。
 */
@Schema(description = "创建知识库请求对象")
public class CreateKnowledgeBaseRequestObject {

    /**
     * 知识库名称。
     */
    @NotBlank(message = "知识库名称不能为空")
    @Schema(description = "知识库名称", example = "产品文档库")
    private String name;

    /**
     * 知识库描述。
     */
    @Schema(description = "知识库描述", example = "用于存储产品需求与设计文档")
    private String description;

    /**
     * 向量提供方。
     */
    @NotBlank(message = "向量提供方不能为空")
    @Schema(description = "向量提供方", example = "openai")
    private String embeddingProvider;

    /**
     * 向量模型。
     */
    @NotBlank(message = "向量模型不能为空")
    @Schema(description = "向量模型", example = "text-embedding-3-small")
    private String embeddingModel;

    /**
     * 获取知识库名称。
     *
     * @return 知识库名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置知识库名称。
     *
     * @param name 知识库名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取知识库描述。
     *
     * @return 知识库描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置知识库描述。
     *
     * @param description 知识库描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取向量提供方。
     *
     * @return 向量提供方
     */
    public String getEmbeddingProvider() {
        return embeddingProvider;
    }

    /**
     * 设置向量提供方。
     *
     * @param embeddingProvider 向量提供方
     */
    public void setEmbeddingProvider(String embeddingProvider) {
        this.embeddingProvider = embeddingProvider;
    }

    /**
     * 获取向量模型。
     *
     * @return 向量模型
     */
    public String getEmbeddingModel() {
        return embeddingModel;
    }

    /**
     * 设置向量模型。
     *
     * @param embeddingModel 向量模型
     */
    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }
}
