package com.bubua12.atlas.auth.service.impl;

import com.bubua12.atlas.api.auth.constant.AuthCacheConstant;
import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.auth.converter.AuthConverter;
import com.bubua12.atlas.auth.exception.AuthErrorCode;
import com.bubua12.atlas.auth.exception.AuthException;
import com.bubua12.atlas.auth.entity.request.LoginRequest;
import com.bubua12.atlas.auth.handler.LoginHandlerFactory;
import com.bubua12.atlas.auth.service.AuthService;
import com.bubua12.atlas.auth.service.LoginFailRecordService;
import com.bubua12.atlas.auth.utils.AuthUtils;
import com.bubua12.atlas.auth.entity.vo.LoginVO;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.utils.JwtUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 * 核心职责：通过策略模式委托认证，认证通过后生成 JWT 并缓存到 Redis。
 */
@Slf4j
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
    @Resource
    private LoginFailRecordService loginFailRecordService;
    @Resource
    private AuthUtils authUtils;
    @Resource
    private AuthConverter authConverter;

    /**
     * JWT 过期时间（秒），从配置文件读取
     */
    @Value("${atlas.jwt.expiration}")
    private long expiration;

    /**
     * 获取随机化的过期时间，防止缓存雪崩
     * 基础 TTL ± 10% 随机偏移
     *
     * @return 随机化后的过期时间（秒）
     */
    private long getRandomizedExpiration() {
        // 7200s ± 10% = 6480-7920s
        double randomFactor = 0.9 + (Math.random() * 0.2);  // 0.9 - 1.1
        return (long) (expiration * randomFactor);
    }

    /**
     * 统一登录流程：
     * 1. 检查 IP 和账号是否被锁定
     * 2. 根据 grantType 获取对应处理器完成认证
     * 3. 生成 JWT 令牌
     * 4. 构建登录用户信息缓存到 Redis
     * 5. 清除登录失败记录
     */
    @Override
    public LoginVO login(LoginRequest loginRequest, String clientIp) {
        String username = loginRequest.getUsername();

        // Pipeline 一次网络往返检查 IP + 账号锁定
        if (clientIp != null && username != null) {
            authUtils.checkLoginLock(clientIp, username);
        }

        try {
            // 委托给对应的登录处理器完成认证
            UserDTO user = loginHandlerFactory
                    .getHandler(loginRequest.getGrantType())
                    .authenticate(loginRequest);

            String token = jwtUtils.generateToken(user.getUserId(), user.getUsername());

            LoginUser loginUser = authConverter.po2vo(user);
            loginUser.setToken(token);
            loginUser.setClientIp(clientIp);

            log.info("当前登录用户存入Redis => {}", loginUser);

            // 使用随机化的 TTL 防止缓存雪崩
            long ttl = getRandomizedExpiration();
            redisService.set(AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + token, loginUser, ttl, TimeUnit.SECONDS);

            // 登录成功，清除失败记录
            if (clientIp != null && username != null) {
                loginFailRecordService.clearLoginFail(clientIp, username);
            }

            LoginVO loginVO = new LoginVO();
            loginVO.setToken(token);
            loginVO.setExpiresIn(ttl);  // 返回实际过期时间
            return loginVO;
        } catch (Exception e) {
            // 登录失败，记录失败次数
            if (clientIp != null && username != null) {
                loginFailRecordService.recordLoginFail(clientIp, username);
            }
            throw e;
        }
    }

    /**
     * 登出：删除 Redis 中的登录状态
     */
    @Override
    public void logout(String token) {
        redisService.delete(AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + token);
    }

    /**
     * 刷新令牌：校验旧 token，生成新 token 并更新 Redis 缓存
     * 优化：先生成新 Token 再删除旧 Token，避免竞态条件
     */
    @Override
    public LoginVO refreshToken(String token) {
        if (jwtUtils.isTokenExpired(token)) {
            throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
        }
        
        Long userId = jwtUtils.getUserId(token);
        String username = jwtUtils.getUsername(token);
        
        // 获取旧 Token 的用户信息
        LoginUser loginUser = redisService.get(AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + token);
        if (loginUser == null) {
            throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
        }
        
        // 先生成新 Token 并写入 Redis
        String newToken = jwtUtils.generateToken(userId, username);
        loginUser.setToken(newToken);
        
        long ttl = getRandomizedExpiration();
        redisService.set(AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + newToken, 
                         loginUser, ttl, TimeUnit.SECONDS);
        
        // 再删除旧 Token（即使删除失败，旧 Token 也会自然过期）
        try {
            redisService.delete(AuthCacheConstant.AUTH_TOKEN_CACHE_PREFIX + token);
        } catch (Exception e) {
            log.warn("删除旧 Token 失败，将自然过期: {}", token, e);
        }
        
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(newToken);
        loginVO.setExpiresIn(ttl);
        return loginVO;
    }
}
