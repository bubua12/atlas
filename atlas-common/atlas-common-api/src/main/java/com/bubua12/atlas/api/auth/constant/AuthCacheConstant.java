package com.bubua12.atlas.api.auth.constant;

/**
 * 认证模块缓存常量
 *
 * @author bubua12
 * @since 2026/03/21 22:30
 */
public interface AuthCacheConstant {

    /** IP 锁定标记前缀 */
    String AUTH_LOCK_IP_PREFIX = "auth:lock:ip:";

    /** 账号锁定标记前缀 */
    String AUTH_LOCK_ACCOUNT_PREFIX = "auth:lock:account:";

    /** IP 登录失败计数前缀 */
    String AUTH_FAIL_IP_PREFIX = "auth:fail:ip:";

    /** 账号登录失败计数前缀 */
    String AUTH_FAIL_ACCOUNT_PREFIX = "auth:fail:account:";

    /** Redis中登录令牌的key前缀 */
    String AUTH_TOKEN_CACHE_PREFIX = "auth:token:";

    /** 按用户索引登录令牌集合的key前缀 */
    String AUTH_USER_TOKEN_SET_PREFIX = "auth:user:tokens:";

    /** 权限变更通知频道 */
    String AUTH_PERMISSION_CHANGE_CHANNEL = "auth:permission:change";
}
