package com.bubua12.atlas.auth.handler;

import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.auth.feign.AtlasSystemFeign;
import com.bubua12.atlas.auth.entity.request.LoginRequest;
import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.redis.service.RedisService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 短信验证码登录处理器
 * 从 Redis 中校验验证码，通过后根据手机号查询用户信息。
 */
@Component
public class CaptchaLoginHandler implements LoginHandler {

    @Resource
    private RedisService redisService;
    @Resource
    private AtlasSystemFeign atlasSystemFeign;

    /**
     * Redis 中短信验证码的 key 前缀
     */
    private static final String SMS_CODE_PREFIX = "sms:code:";

    @Override
    public String grantType() {
        return "captcha";
    }

    /**
     * 验证码登录认证流程：
     * 1. 从 Redis 获取缓存的验证码并校验
     * 2. 校验通过后删除验证码（防止重复使用）
     * 3. 根据手机号远程查询用户信息
     */
    @Override
    public UserDTO authenticate(LoginRequest request) {
        String cacheCode = redisService.get(SMS_CODE_PREFIX + request.getPhone());
        if (cacheCode == null) {
            throw new RuntimeException("验证码已过期");
        }
        if (!cacheCode.equals(request.getCaptchaCode())) {
            throw new RuntimeException("验证码错误");
        }
        // 验证通过，删除已使用的验证码
        redisService.delete(SMS_CODE_PREFIX + request.getPhone());

        CommonResult<UserDTO> result = atlasSystemFeign.getUserByPhone(request.getPhone());
        if (result == null || result.getData() == null) {
            throw new RuntimeException("该手机号未绑定用户");
        }
        return result.getData();
    }
}
