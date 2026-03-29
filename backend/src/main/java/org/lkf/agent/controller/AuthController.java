package org.lkf.agent.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lkf.agent.common.dto.ApiResponseObject;
import org.lkf.agent.dto.RegisterRequestObject;
import org.lkf.agent.service.AuthAppService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.lkf.agent.common.util.JwtTokenUtil;
import org.lkf.agent.dto.LoginRequestObject;
import org.lkf.agent.dto.TokenResponseObject;

/**
 * 认证控制器。
 */
@RestController
@Validated
@RequestMapping("/api/auth")
@Tag(name = "认证接口")
public class AuthController {

    /**
     * 认证应用服务。
     */
    private final AuthAppService authAppService;

    /**
     * 构造器。
     *
     * @param authAppService 认证应用服务
     */
    public AuthController(AuthAppService authAppService) {
        this.authAppService = authAppService;
    }

    /**
     * 注册接口。
     *
     * @param requestObject 注册请求对象
     * @return 通用响应
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新的用户账号")
    public ApiResponseObject<Void> register(@Valid @RequestBody RegisterRequestObject requestObject) {
        authAppService.register(requestObject);
        return ApiResponseObject.success(null);
    }

    /**
     * 登录接口。
     *
     * @param requestObject 登录请求对象
     * @return token响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "校验用户名密码并返回访问令牌")
    public ApiResponseObject<TokenResponseObject> login(@Valid @RequestBody LoginRequestObject requestObject) {
        String username = authAppService.login(requestObject);
        return ApiResponseObject.success(new TokenResponseObject(JwtTokenUtil.generateToken(username)));
    }
}
