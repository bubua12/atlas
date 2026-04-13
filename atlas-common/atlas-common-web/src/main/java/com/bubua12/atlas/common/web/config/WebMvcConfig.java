package com.bubua12.atlas.common.web.config;

import com.bubua12.atlas.common.web.interceptor.GatewaySignatureInterceptor;
import com.bubua12.atlas.common.web.interceptor.UserContextInterceptor;
import com.bubua12.atlas.common.web.resolver.ClientIpArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置
 * 注册拦截器和参数解析器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 网关签名验证拦截器（最高优先级）
        registry.addInterceptor(new GatewaySignatureInterceptor())
                .addPathPatterns("/**")
                .order(0);
        
        // 2. 用户上下文拦截器
        registry.addInterceptor(new UserContextInterceptor(objectMapper))
                .addPathPatterns("/**")
                .order(1);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ClientIpArgumentResolver());
    }
}
