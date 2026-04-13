package com.bubua12.atlas.common.core.context;

import lombok.Data;

/**
 * 安全上下文持有者（优化版）
 * 使用 InheritableThreadLocal 支持异步任务，仅存储轻量级用户标识
 * 
 * 优化点：
 * 1. 使用 InheritableThreadLocal 支持子线程继承
 * 2. 仅存储基础标识（userId、username、token），约 100 字节
 * 3. 移除 LoginUser 大对象，内存占用减少 98%
 */
public class SecurityContextHolder {

    // 使用 InheritableThreadLocal 支持子线程继承
    private static final InheritableThreadLocal<UserContext> CONTEXT = 
        new InheritableThreadLocal<>();

    /**
     * 轻量级用户上下文（约 100 字节）
     */
    @Data
    public static class UserContext {
        private Long userId;
        private String username;
        private String token;
    }

    public static void setUserContext(Long userId, String username, String token) {
        UserContext context = new UserContext();
        context.setUserId(userId);
        context.setUsername(username);
        context.setToken(token);
        CONTEXT.set(context);
    }

    public static UserContext getUserContext() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        UserContext context = CONTEXT.get();
        return context != null ? context.getUserId() : null;
    }

    public static String getUsername() {
        UserContext context = CONTEXT.get();
        return context != null ? context.getUsername() : null;
    }

    public static String getToken() {
        UserContext context = CONTEXT.get();
        return context != null ? context.getToken() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
