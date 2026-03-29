package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 知识库响应对象。
 */
@Schema(description = "知识库响应对象")
public class KnowledgeBaseResponseObject {

    /**
     * 知识库ID。
     */
    @Schema(description = "知识库ID", example = "kb_01HXYZ")
    private String id;

    /**
     * 知识库名称。
     */
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
    @Schema(description = "向量提供方", example = "openai")
    private String embeddingProvider;

    /**
     * 向量模型。
     */
    @Schema(description = "向量模型", example = "text-embedding-3-small")
    private String embeddingModel;

    /**
     * 状态。
     */
    @Schema(description = "状态", example = "ACTIVE")
    private String status;

    /**
     * 默认构造器。
     */
    public KnowledgeBaseResponseObject() {
    }

    /**
     * 构造器。
     *
     * @param id 知识库ID
     * @param name 知识库名称
     * @param description 知识库描述
     * @param embeddingProvider 向量提供方
     * @param embeddingModel 向量模型
     * @param status 状态
     */
    public KnowledgeBaseResponseObject(String id, String name, String description, String embeddingProvider,
                                       String embeddingModel, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.embeddingProvider = embeddingProvider;
        this.embeddingModel = embeddingModel;
        this.status = status;
    }

    /**
     * 获取知识库ID。
     *
     * @return 知识库ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置知识库ID。
     *
     * @param id 知识库ID
     */
    public void setId(String id) {
        this.id = id;
    }

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

    /**
     * 获取状态。
     *
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态。
     *
     * @param status 状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
