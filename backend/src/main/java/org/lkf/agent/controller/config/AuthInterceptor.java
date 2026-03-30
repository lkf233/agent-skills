package org.lkf.agent.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.lkf.agent.common.context.UserContext;
import org.lkf.agent.common.exception.BusinessException;
import org.lkf.agent.entity.UserAccountEntity;
import org.lkf.agent.service.AuthAppService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.lkf.agent.common.util.JwtTokenUtil;
import org.springframework.http.HttpMethod;

/**
 * 认证拦截器。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * 认证应用服务。
     */
    private final AuthAppService authAppService;

    /**
     * 构造器。
     *
     * @param authAppService 认证应用服务
     */
    public AuthInterceptor(AuthAppService authAppService) {
        this.authAppService = authAppService;
    }

    /**
     * 请求前置处理。
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @return 是否继续执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或令牌无效");
        }
        String token = authorization.substring("Bearer ".length());
        String username = JwtTokenUtil.parseUsername(token);
        UserAccountEntity userAccountEntity = authAppService.getUserByUsername(username);
        UserContext.setCurrentUser(userAccountEntity.getId(), userAccountEntity.getUsername());
        return true;
    }

    /**
     * 请求完成清理上下文。
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @param ex 异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
