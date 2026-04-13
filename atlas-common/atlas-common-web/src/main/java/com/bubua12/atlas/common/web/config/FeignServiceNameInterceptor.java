package com.bubua12.atlas.common.web.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * Feign 拦截器：自动添加 X-Service-Name 请求头
 * 供下游服务的 @InternalApi 切面验证调用方身份
 */
@Component
@ConditionalOnClass(RequestInterceptor.class)
public class FeignServiceNameInterceptor implements RequestInterceptor {

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-Service-Name", serviceName);
    }
}
