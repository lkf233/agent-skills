package org.lkf.agent.entity;

import java.time.LocalDateTime;

/**
 * 会话实体。
 */
public class ConversationEntity {

    /**
     * 会话主键ID。
     */
    private String id;

    /**
     * 所属用户ID。
     */
    private Long userId;

    private String agentId;

    /**
     * 会话标题。
     */
    private String title;

    /**
     * 会话状态。
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
     * 伪删除标记：0-未删除，1-已删除。
     */
    private Integer delFlag;

    /**
     * 获取会话主键ID。
     *
     * @return 会话主键ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置会话主键ID。
     *
     * @param id 会话主键ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取所属用户ID。
     *
     * @return 所属用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置所属用户ID。
     *
     * @param userId 所属用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * 获取会话标题。
     *
     * @return 会话标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置会话标题。
     *
     * @param title 会话标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取会话状态。
     *
     * @return 会话状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置会话状态。
     *
     * @param status 会话状态
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
