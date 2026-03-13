package com.bubua12.atlas.common.web.interceptor;

import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 * 从请求头中获取用户信息并存入 ThreadLocal
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取网关透传的用户信息
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");
        // 网关会将原始 Token 透传
        String token = request.getHeader("Authorization");

        if (userId != null) {
            SecurityContextHolder.setUserId(Long.valueOf(userId));
        }
        if (username != null) {
            SecurityContextHolder.setUsername(username);
        }
        if (token != null) {
            SecurityContextHolder.setToken(token);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求处理完成后清理 ThreadLocal，防止内存泄漏
        SecurityContextHolder.clear();
    }
}
