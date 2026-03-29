package org.lkf.agent.entity;

import java.time.LocalDateTime;

/**
 * 用户账户实体。
 */
public class UserAccountEntity {

    /**
     * 用户主键ID。
     */
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 密码哈希值。
     */
    private String passwordHash;

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
     * 获取用户主键ID。
     *
     * @return 用户主键ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置用户主键ID。
     *
     * @param id 用户主键ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码哈希值。
     *
     * @return 密码哈希值
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 设置密码哈希值。
     *
     * @param passwordHash 密码哈希值
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
