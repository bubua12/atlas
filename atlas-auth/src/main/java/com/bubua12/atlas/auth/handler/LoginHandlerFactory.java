package com.bubua12.atlas.auth.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 登录处理器工厂
 * Spring 启动时自动收集所有 LoginHandler 实现，按 grantType 建立路由映射。
 * 调用 getHandler("password") 即可获取 PasswordLoginHandler 实例。
 */
@Component
public class LoginHandlerFactory {

    /**
     * grantType -> LoginHandler 的路由表
     */
    private final Map<String, LoginHandler> handlerMap;

    @Autowired
    public LoginHandlerFactory(List<LoginHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(LoginHandler::grantType, Function.identity()));
    }

    public LoginHandler getHandler(String grantType) {
        LoginHandler handler = handlerMap.get(grantType);
        if (handler == null) {
            throw new RuntimeException("不支持的登录方式: " + grantType);
        }
        return handler;
    }
}
