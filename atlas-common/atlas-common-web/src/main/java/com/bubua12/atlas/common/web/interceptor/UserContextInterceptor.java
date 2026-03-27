package com.bubua12.atlas.common.web.interceptor;

import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.redis.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 * 从请求头中获取用户信息并存入 ThreadLocal，
 * 同时从 Redis 取出完整的 LoginUser 存入上下文，
 * 供数据权限等组件使用。
 */
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private static final String TOKEN_CACHE_PREFIX = "auth:token:";

    private final RedisService redisService;

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

            // fixme 线程上下文塞实体类会不会比较臃肿？影响性能之类的？还是说实时查询是哪个用户？
            // 从 Redis 获取完整的 LoginUser（含 deptId、dataScope 等）
            String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            LoginUser loginUser = redisService.get(TOKEN_CACHE_PREFIX + rawToken);
            if (loginUser != null) {
                SecurityContextHolder.setLoginUser(loginUser);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求处理完成后清理 ThreadLocal，防止内存泄漏
        SecurityContextHolder.clear();
    }
}
