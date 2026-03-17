package com.bubua12.atlas.auth.controller;

import cn.hutool.http.HttpUtil;
import com.bubua12.atlas.api.system.dto.SysUserDTO;
import com.bubua12.atlas.auth.feign.AtlasSystemFeign;
import com.bubua12.atlas.common.core.result.CommonResult;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求  todo 对接微博登录
 *
 * @author bubua12
 * @since 2026/3/10 10:19
 */
@RestController
@RequestMapping("/")
public class OAuth2Controller {

    @Resource
    private AtlasSystemFeign atlasSystemFeign;

    @Value("${atlas.weibo.client-id}")
    private String weiboClientId;

    @Value("${atlas.weibo.client-secret}")
    private String weiboClientSecret;

    @Value("${atlas.weibo.redirect-uri}")
    private String weiboRedirectUri;


    @GetMapping("/oauth2/weibo/success")
    public String weibo(@RequestParam("code") String code) {
        // 1、根据code换取 accessToken
        String postUrl = "https://api.weibo.com/oauth2/access_token";

        Map<String, Object> params = new HashMap<>();
        params.put("client_id", weiboClientId);
        params.put("client_secret", weiboClientSecret);
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", weiboRedirectUri);

        // 发送请求
        String response = HttpUtil.post(postUrl, params);
        System.out.println(response);

        String accessToken = "";

        // 知道当前是哪个社交用户
        // 1、登录或者注册这个社交用户：当前用户如果是第一次进网站，自动注册进来 -- 为当前社交用户生成一个用户账号 -- 建立设计用户账号与系统用户的关联
        // 2、


        // 2、登录成功就跳回首页

        return "";
    }


    // 社交用户登录：登录和注册合并
    @GetMapping("/oauth2/login")
    public CommonResult<Void> oauthLogin() {

        // 登录或者注册这个社交用户
        SysUserDTO sysUserDTO = new SysUserDTO();
        atlasSystemFeign.createUserOAuth2(sysUserDTO);

        return atlasSystemFeign.createUserOAuth2(sysUserDTO);
    }
}
