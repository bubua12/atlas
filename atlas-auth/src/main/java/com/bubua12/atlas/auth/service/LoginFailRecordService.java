package com.bubua12.atlas.auth.service;

/**
 * 登录失败记录服务
 */
public interface LoginFailRecordService {

    /**
     * 检查 IP 是否被锁定
     */
    boolean isIpLocked(String ip);

    /**
     * 检查账号是否被锁定
     */
    boolean isAccountLocked(String username);

    /**
     * 记录登录失败
     */
    void recordLoginFail(String ip, String username);

    /**
     * 清除登录失败记录
     */
    void clearLoginFail(String ip, String username);
}
