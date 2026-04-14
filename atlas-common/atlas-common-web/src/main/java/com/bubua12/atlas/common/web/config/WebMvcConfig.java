package com.bubua12.atlas.common.web.config;

import com.bubua12.atlas.common.web.interceptor.RequestSecurityInterceptor;
import com.bubua12.atlas.common.web.interceptor.UserContextInterceptor;
import com.bubua12.atlas.common.web.resolver.ClientIpArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    @Value("${atlas.security.request-signature.secret:${ATLAS_REQUEST_SIGNATURE_SECRET:atlas-request-signature-secret-change-me}}")
    private String requestSignatureSecret;

    @Value("${atlas.security.request-signature.allowed-clock-skew-millis:${ATLAS_REQUEST_SIGNATURE_ALLOWED_SKEW_MILLIS:300000}}")
    private long allowedClockSkewMillis;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestSecurityInterceptor(requestSignatureSecret, allowedClockSkewMillis))
                .addPathPatterns("/**");
        registry.addInterceptor(new UserContextInterceptor(objectMapper))
                .addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ClientIpArgumentResolver());
    }
}
