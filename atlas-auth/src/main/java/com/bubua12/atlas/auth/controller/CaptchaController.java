package com.bubua12.atlas.auth.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.redis.service.RedisService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码控制器
 * 提供发送短信验证码接口，验证码存入 Redis 并设置过期时间。
 */
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private RedisService redisService;

    /**
     * Redis 中短信验证码的 key 前缀
     */
    private static final String SMS_CODE_PREFIX = "sms:code:";
    /**
     * 验证码有效期（分钟）
     */
    private static final long EXPIRE_MINUTES = 5;

    /**
     * 发送短信验证码
     * 生成6位随机验证码存入 Redis，返回 captchaKey 和过期时间
     *
     * @param phone 手机号
     */
    @GetMapping("/sms")
    public CommonResult<Map<String, Object>> sendSms(@RequestParam("phone") String phone) {
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        String key = UUID.randomUUID().toString();
        redisService.set(SMS_CODE_PREFIX + phone, code, EXPIRE_MINUTES, TimeUnit.MINUTES);
        // TODO: 对接真实短信服务发送验证码
        return CommonResult.success(Map.of("captchaKey", key, "expireIn", EXPIRE_MINUTES * 60));
    }
}
