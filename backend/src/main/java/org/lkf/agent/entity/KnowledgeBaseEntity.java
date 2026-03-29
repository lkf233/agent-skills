package org.lkf.agent.entity;

import java.time.LocalDateTime;

/**
 * 知识库实体。
 */
public class KnowledgeBaseEntity {

    /**
     * 知识库ID。
     */
    private String id;

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 名称。
     */
    private String name;

    /**
     * 描述。
     */
    private String description;

    /**
     * 向量提供方。
     */
    private String embeddingProvider;

    /**
     * 向量模型。
     */
    private String embeddingModel;

    /**
     * 状态。
     */
    private String status;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 伪删除标记。
     */
    private Integer delFlag;

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
     * 获取用户ID。
     *
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户ID。
     *
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取名称。
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称。
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取描述。
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置描述。
     *
     * @param description 描述
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

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间。
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 获取伪删除标记。
     *
     * @return 伪删除标记
     */
    public Integer getDelFlag() {
        return delFlag;
    }

    /**
     * 设置伪删除标记。
     *
     * @param delFlag 伪删除标记
     */
    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }
}
