package com.bubua12.atlas.common.web.config;

import com.bubua12.atlas.common.security.service.RequestSignatureService;
import com.bubua12.atlas.common.web.interceptor.GatewayIdentityInterceptor;
import com.bubua12.atlas.common.web.interceptor.UserContextInterceptor;
import com.bubua12.atlas.common.web.resolver.ClientIpArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestSignatureService requestSignatureService;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 先验签，再建立线程上下文；顺序反过来就会重新把未经验证的身份写回 SecurityContextHolder。
        registry.addInterceptor(new GatewayIdentityInterceptor(requestSignatureService))
                .addPathPatterns("/**")
                .order(0);

        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/**")
                .order(1);
    }

    /**
     * fixme 学习这块内容
     *
     * @param resolvers initially an empty list
     * @see ClientIpArgumentResolver
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // @ClientIp 这类参数解析依然保留，不受新增安全链路影响。
        resolvers.add(new ClientIpArgumentResolver());
    }
}
