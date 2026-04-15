package com.bubua12.atlas.common.web.interceptor;

import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import com.bubua12.atlas.common.core.model.GatewayUserContext;
import com.bubua12.atlas.common.core.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.bubua12.atlas.common.core.constant.RequestHeaderConstants.ATTR_GATEWAY_USER_CONTEXT;
import static com.bubua12.atlas.common.core.constant.RequestHeaderConstants.AUTHORIZATION;

/**
 * 用户上下文拦截器。
 *
 * <p>这个拦截器只消费已经被 {@link GatewayIdentityInterceptor} 验证过的 request attribute，
 * 不再直接信任原始请求头里的用户信息，从而把“可伪造输入”和“可信身份上下文”隔离开。
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        String token = request.getHeader(AUTHORIZATION);
        if (token != null && !token.isBlank()) {
            // 保留原始 Authorization，方便现有权限切面和兼容逻辑继续工作。
            SecurityContextHolder.setToken(token);
        }

        Object gatewayUserContext = request.getAttribute(ATTR_GATEWAY_USER_CONTEXT);
        if (gatewayUserContext instanceof GatewayUserContext userContext) {
            // 只有经过验签的用户断言才会被恢复成统一登录态，供权限和数据权限逻辑复用。
            LoginUser loginUser = userContext.toLoginUser();
            if (loginUser.getUserId() != null) {
                SecurityContextHolder.setUserId(loginUser.getUserId());
            }
            if (loginUser.getUsername() != null) {
                SecurityContextHolder.setUsername(loginUser.getUsername());
            }
            SecurityContextHolder.setLoginUser(loginUser);
        }
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex
    ) {
        // ThreadLocal 生命周期必须跟随一次 HTTP 请求结束，避免线程复用时串用户。
        SecurityContextHolder.clear();
    }
}
