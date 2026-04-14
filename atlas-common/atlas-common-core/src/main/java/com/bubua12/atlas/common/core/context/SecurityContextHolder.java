package com.bubua12.atlas.common.core.context;

import com.bubua12.atlas.common.core.model.LoginUser;

/**
 * 安全上下文持有者
 * 基于 ThreadLocal 存储当前请求的用户信息（userId、username、token），
 * 供业务层获取当前登录用户。
 */
public class SecurityContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        getOrCreateContext().setUserId(userId);
    }

    public static Long getUserId() {
        UserContext userContext = CONTEXT.get();
        return userContext == null ? null : userContext.getUserId();
    }

    public static void setUsername(String username) {
        getOrCreateContext().setUsername(username);
    }

    public static String getUsername() {
        UserContext userContext = CONTEXT.get();
        return userContext == null ? null : userContext.getUsername();
    }

    public static void setToken(String token) {
        getOrCreateContext().setToken(token);
    }

    public static String getToken() {
        UserContext userContext = CONTEXT.get();
        return userContext == null ? null : userContext.getToken();
    }

    public static void setLoginUser(LoginUser loginUser) {
        getOrCreateContext().setLoginUser(loginUser);
    }

    public static LoginUser getLoginUser() {
        UserContext userContext = CONTEXT.get();
        return userContext == null ? null : userContext.getLoginUser();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static UserContext getContext() {
        return CONTEXT.get();
    }

    public static UserContext copyContext() {
        UserContext userContext = CONTEXT.get();
        return copyOf(userContext);
    }

    public static void setContext(UserContext userContext) {
        if (userContext == null) {
            clear();
            return;
        }
        CONTEXT.set(copyOf(userContext));
    }

    private static UserContext getOrCreateContext() {
        UserContext userContext = CONTEXT.get();
        if (userContext == null) {
            userContext = new UserContext();
            CONTEXT.set(userContext);
        }
        return userContext;
    }

    private static UserContext copyOf(UserContext source) {
        if (source == null) {
            return null;
        }
        UserContext target = new UserContext();
        target.setUserId(source.getUserId());
        target.setUsername(source.getUsername());
        target.setToken(source.getToken());
        target.setLoginUser(source.getLoginUser());
        return target;
    }

    /**
     * 当前请求的安全上下文。
     */
    public static class UserContext {

        private Long userId;

        private String username;

        private String token;

        private LoginUser loginUser;

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

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public LoginUser getLoginUser() {
            return loginUser;
        }

        public void setLoginUser(LoginUser loginUser) {
            this.loginUser = loginUser;
        }
    }
}
