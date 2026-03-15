package com.bubua12.atlas.auth.controller;

import com.bubua12.atlas.auth.form.LoginRequest;
import com.bubua12.atlas.auth.service.AuthService;
import com.bubua12.atlas.auth.vo.LoginVO;
import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.log.annotation.OperLog;
import com.bubua12.atlas.common.redis.annotation.RedisLimit;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 提供统一登录、登出、刷新令牌接口。
 * 登录时通过 grantType 字段路由到对应的登录处理器。
 */
@RestController
@RequestMapping("/")
public class AuthController {

    @Resource
    private AuthService authService;

    @Value("${atlas.wecom.corp-id:}")
    private String corpId;

    @Value("${atlas.wecom.agent-id:}")
    private String agentId;

    @Value("${atlas.wecom.redirect-uri:}")
    private String redirectUri;

    /**
     * 统一登录入口，根据 grantType 分发到不同登录方式
     * fixme 接口限流
     */
    @PostMapping("/login")
    @RedisLimit(key = "login", permitsPerSecond = 5)
    @OperLog(title = "登录", businessType = "读取") // fixme 规范化配置
    public CommonResult<LoginVO> login(@RequestBody LoginRequest loginRequest) {
        LoginVO loginVO = authService.login(loginRequest);
        return CommonResult.success(loginVO);
    }

    /**
     * 登出，清除 Redis 中的登录状态
     */
    @PostMapping("/logout")
    public CommonResult<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return CommonResult.success();
    }

    /**
     * 刷新令牌，生成新 token 并续期
     */
    @PostMapping("/refresh")
    public CommonResult<LoginVO> refresh(@RequestHeader("Authorization") String token) {
        LoginVO loginVO = authService.refreshToken(token);
        return CommonResult.success(loginVO);
    }

    /**
     * 获取企业微信扫码登录参数（corpId、agentId、redirectUri）
     */
    @GetMapping("/wecom/config")
    public CommonResult<Map<String, String>> wecomConfig() {
        return CommonResult.success(Map.of(
                "corpId", corpId,
                "agentId", agentId,
                "redirectUri", redirectUri
        ));
    }
}
