package com.bubua12.atlas.auth.handler;

import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.auth.feign.AtlasSystemFeign;
import com.bubua12.atlas.auth.form.LoginRequest;
import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.redis.service.RedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信扫码登录处理器
 * 用前端传来的 code 换取企业微信 UserId，再匹配系统用户。
 */
@Slf4j
@Component
public class WechatLoginHandler implements LoginHandler {

    @Resource
    private AtlasSystemFeign atlasSystemFeign;

    @Resource
    private RedisService redisService;

    @Value("${atlas.wecom.corp-id:}")
    private String corpId;

    @Value("${atlas.wecom.secret:}")
    private String secret;

    private static final String TOKEN_URL =
            "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";

    private static final String USER_INFO_URL =
            "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo?access_token=%s&code=%s";

    private static final String TOKEN_CACHE_KEY = "wecom:access_token";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String grantType() {
        return "wechat";
    }

    @Override
    public UserDTO authenticate(LoginRequest request) {
        String accessToken = getAccessToken();

        // 用 code 换取企业微信 UserId
        String url = String.format(USER_INFO_URL, accessToken, request.getWxCode());
        Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

        if (resp == null || !Integer.valueOf(0).equals(resp.get("errcode"))) {
            String errMsg = resp != null ? (String) resp.get("errmsg") : "无响应";
            throw new RuntimeException("企业微信登录失败: " + errMsg);
        }

        String userId = (String) resp.get("userid");
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("该企业微信账号非企业内部成员");
        }

        // 用企业微信 UserId 查询绑定的系统用户
        CommonResult<UserDTO> result = atlasSystemFeign.getUserByOpenId(userId);
        if (result == null || result.getData() == null) {
            throw new RuntimeException("该企业微信账号未绑定系统用户");
        }
        return result.getData();
    }

    /**
     * 获取 access_token，优先从 Redis 缓存读取
     */
    private String getAccessToken() {
        String cached = redisService.get(TOKEN_CACHE_KEY);
        if (cached != null) {
            return cached;
        }

        String url = String.format(TOKEN_URL, corpId, secret);
        Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

        if (resp == null || !Integer.valueOf(0).equals(resp.get("errcode"))) {
            String errMsg = resp != null ? (String) resp.get("errmsg") : "无响应";
            throw new RuntimeException("获取企业微信 access_token 失败: " + errMsg);
        }

        String token = (String) resp.get("access_token");
        int expiresIn = (int) resp.get("expires_in");
        // 提前 200 秒过期，避免边界问题
        redisService.set(TOKEN_CACHE_KEY, token, expiresIn - 200, TimeUnit.SECONDS);
        return token;
    }
}
