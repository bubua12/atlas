package com.bubua12.atlas.auth.handler;

import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.auth.form.LoginRequest;

/**
 * 登录策略接口
 * 每种登录方式实现此接口，由 Spring 自动注册到 LoginHandlerFactory。
 * 新增登录方式只需新建一个 @Component 实现类，零改动其他代码。
 */
public interface LoginHandler {

    /**
     * 返回该处理器支持的授权类型（如 "password"、"captcha"、"wechat"）
     */
    String grantType();

    /**
     * 执行认证逻辑，校验通过后返回用户信息
     *
     * @param request 统一登录请求
     * @return 认证通过的用户信息，交由 AuthServiceImpl 生成 JWT 并存入 Redis
     */
    UserDTO authenticate(LoginRequest request);
}
