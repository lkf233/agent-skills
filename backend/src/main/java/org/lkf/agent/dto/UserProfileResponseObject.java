package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "当前用户信息响应对象")
public class UserProfileResponseObject {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "alice")
    private String username;

    public UserProfileResponseObject() {
    }

    public UserProfileResponseObject(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
