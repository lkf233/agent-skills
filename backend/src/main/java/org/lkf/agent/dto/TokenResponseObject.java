package org.lkf.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Token 响应对象。
 */
@Schema(description = "登录令牌响应对象")
public class TokenResponseObject {

    /**
     * 访问令牌。
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    /**
     * 默认构造器。
     */
    public TokenResponseObject() {
    }

    /**
     * 构造器。
     *
     * @param accessToken 访问令牌
     */
    public TokenResponseObject(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * 获取访问令牌。
     *
     * @return 访问令牌
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 设置访问令牌。
     *
     * @param accessToken 访问令牌
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
