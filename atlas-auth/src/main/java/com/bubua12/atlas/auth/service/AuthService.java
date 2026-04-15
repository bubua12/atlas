package com.bubua12.atlas.auth.service;

import com.bubua12.atlas.auth.entity.request.LoginRequest;
import com.bubua12.atlas.auth.entity.vo.LoginVO;

/**
 * 认证服务接口
 * 定义登录、登出、刷新令牌等核心认证操作。
 */
public interface AuthService {

    /**
     * 统一登录，根据 grantType 路由到对应处理器完成认证
     *
     * @param loginRequest 登录请求参数
     * @param clientIp     客户端真实IP地址
     */
    LoginVO login(LoginRequest loginRequest, String clientIp);

    /**
     * 登出，清除 Redis 中的登录状态
     */
    void logout(String token);

    /**
     * 刷新令牌，生成新 token 并续期
     */
    LoginVO refreshToken(String token);
}
