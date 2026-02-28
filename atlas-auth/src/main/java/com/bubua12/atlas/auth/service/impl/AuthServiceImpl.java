package com.bubua12.atlas.auth.service.impl;

import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.auth.form.LoginRequest;
import com.bubua12.atlas.auth.handler.LoginHandlerFactory;
import com.bubua12.atlas.auth.service.AuthService;
import com.bubua12.atlas.auth.util.JwtUtils;
import com.bubua12.atlas.auth.vo.LoginVO;
import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.model.LoginUser;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 * 核心职责：通过策略模式委托认证，认证通过后生成 JWT 并缓存到 Redis。
 */
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * 登录处理器工厂，根据 grantType 路由到对应处理器
     */
    @Resource
    private LoginHandlerFactory loginHandlerFactory;
    @Resource
    private RedisService redisService;
    @Resource
    private JwtUtils jwtUtils;

    /**
     * JWT 过期时间（秒），从配置文件读取
     */
    @Value("${atlas.jwt.expiration}")
    private long expiration;

    /**
     * Redis 中登录令牌的 key 前缀
     */
    private static final String TOKEN_CACHE_PREFIX = "auth:token:";

    /**
     * 统一登录流程：
     * 1. 根据 grantType 获取对应处理器完成认证
     * 2. 生成 JWT 令牌
     * 3. 构建登录用户信息缓存到 Redis
     */
    @Override
    public LoginVO login(LoginRequest loginRequest) {
        // 委托给对应的登录处理器完成认证
        UserDTO user = loginHandlerFactory
                .getHandler(loginRequest.getGrantType())
                .authenticate(loginRequest);

        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUsername(user.getUsername());
        loginUser.setToken(token);
        loginUser.setPermissions(user.getPermissions());
        redisService.set(TOKEN_CACHE_PREFIX + token, loginUser, expiration, TimeUnit.SECONDS);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpiresIn(expiration);
        return loginVO;
    }

    /**
     * 登出：删除 Redis 中的登录状态
     */
    @Override
    public void logout(String token) {
        redisService.delete(TOKEN_CACHE_PREFIX + token);
    }

    /**
     * 刷新令牌：校验旧 token，生成新 token 并更新 Redis 缓存
     */
    @Override
    public LoginVO refreshToken(String token) {
        if (jwtUtils.isTokenExpired(token)) {
            throw new RuntimeException("Token已过期，请重新登录");
        }
        Long userId = jwtUtils.getUserId(token);
        String username = jwtUtils.getUsername(token);
        LoginUser loginUser = redisService.get(TOKEN_CACHE_PREFIX + token);
        redisService.delete(TOKEN_CACHE_PREFIX + token);

        String newToken = jwtUtils.generateToken(userId, username);
        if (loginUser != null) {
            loginUser.setToken(newToken);
            redisService.set(TOKEN_CACHE_PREFIX + newToken, loginUser, expiration, TimeUnit.SECONDS);
        }
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(newToken);
        loginVO.setExpiresIn(expiration);
        return loginVO;
    }
}
