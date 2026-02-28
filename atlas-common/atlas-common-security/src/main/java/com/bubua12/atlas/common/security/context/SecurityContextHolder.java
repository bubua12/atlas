package com.bubua12.atlas.common.security.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全上下文持有者
 * 基于 ThreadLocal 存储当前请求的用户信息（userId、username、token），
 * 供业务层获取当前登录用户。
 */
public class SecurityContextHolder {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "token";

    public static void setUserId(Long userId) {
        CONTEXT.get().put(KEY_USER_ID, userId);
    }

    public static Long getUserId() {
        return (Long) CONTEXT.get().get(KEY_USER_ID);
    }

    public static void setUsername(String username) {
        CONTEXT.get().put(KEY_USERNAME, username);
    }

    public static String getUsername() {
        return (String) CONTEXT.get().get(KEY_USERNAME);
    }

    public static void setToken(String token) {
        CONTEXT.get().put(KEY_TOKEN, token);
    }

    public static String getToken() {
        return (String) CONTEXT.get().get(KEY_TOKEN);
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
