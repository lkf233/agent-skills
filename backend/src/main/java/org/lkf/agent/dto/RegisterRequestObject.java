package org.lkf.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 注册请求对象。
 */
@Schema(description = "注册请求对象")
public class RegisterRequestObject {

    /**
     * 用户名。
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "alice")
    private String username;

    /**
     * 密码。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能小于6位")
    @Schema(description = "密码", example = "123456", minLength = 6)
    private String password;

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
     * 获取密码。
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码。
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
