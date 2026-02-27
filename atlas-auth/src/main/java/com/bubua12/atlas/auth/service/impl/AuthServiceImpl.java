package com.bubua12.atlas.auth.service.impl;

import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.auth.feign.AtlasSystemFeign;
import com.bubua12.atlas.auth.form.LoginBody;
import com.bubua12.atlas.auth.service.AuthService;
import com.bubua12.atlas.auth.util.JwtUtils;
import com.bubua12.atlas.auth.vo.LoginVO;
import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.model.LoginUser;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private AtlasSystemFeign atlasSystemFeign;

    @Resource
    private RedisService redisService;

    @Resource
    private JwtUtils jwtUtils;

    @Value("${atlas.jwt.expiration}")
    private long expiration;

    private static final String TOKEN_CACHE_PREFIX = "auth:token:";

    @Override
    public LoginVO login(LoginBody loginBody) {
        CommonResult<UserDTO> userResult = atlasSystemFeign.getUserByUsername(loginBody.getUsername());
        if (userResult == null || userResult.getData() == null) {
            throw new RuntimeException("用户不存在: " + loginBody.getUsername());
        }
        UserDTO user = userResult.getData();

        // TODO: 生产环境使用 BCryptPasswordEncoder.matches()
        if (!verifyPassword(loginBody.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

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

    @Override
    public void logout(String token) {
        redisService.delete(TOKEN_CACHE_PREFIX + token);
    }

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

    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        // TODO: 使用 BCryptPasswordEncoder.matches(rawPassword, encodedPassword)
        return rawPassword != null && rawPassword.equals(encodedPassword);
    }
}
