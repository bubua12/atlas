package com.bubua12.atlas.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处理社交登录请求  todo 对接微博登录
 *
 * @author bubua12
 * @since 2026/3/10 10:19
 */
@RestController
@RequestMapping("/")
public class OAuth2Controller {


    @GetMapping("/oauth2/weibo/success")
    public String weibo(@RequestParam("code") String code) {

        // 1、根据code换取 accessToken

        String accessToken = "";

        // 知道当前是哪个社交用户
        // 1、登录或者注册这个社交用户：当前用户如果是第一次进网站，自动注册进来 -- 为当前社交用户生成一个用户账号 -- 建立设计用户账号与系统用户的关联
        // 2、



        // 2、登录成功就跳回首页

        return "";
    }


    // 社交用户登录：登录和注册合并
    @GetMapping("/oauth2/login")
    public String oauthLogin() {

        // 登录或者注册这个社交用户

        return "";
    }
}
