package com.bubua12.atlas.common.security.config;

import com.bubua12.atlas.common.security.aspect.InternalApiAspect;
import com.bubua12.atlas.common.security.feign.InternalRequestSigningCapability;
import com.bubua12.atlas.common.security.service.RequestSignatureService;
import com.bubua12.atlas.common.security.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Capability;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Atlas 安全模块自动配置。
 *
 * <p>这里把安全基础设施 bean 集中装配起来，
 * 让业务服务只需要引入模块依赖，就能拿到 JWT、签名验签和内部服务认证能力。
 */
@AutoConfiguration
@EnableConfigurationProperties(AtlasSecurityProperties.class)
@Import(PreAuthorizeAspectConfiguration.class)
public class AtlasSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils() {
        // 允许业务侧按需覆写 JwtUtils，但默认情况下使用公共实现。
        return new JwtUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestSignatureService requestSignatureService(
            AtlasSecurityProperties securityProperties,
            ObjectProvider<ObjectMapper> objectMapperProvider
    ) {
        // 优先复用 Spring 上下文中的 ObjectMapper，避免签名载荷序列化规则与全局配置漂移。
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        return new RequestSignatureService(objectMapper, securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalApiAspect internalApiAspect(RequestSignatureService requestSignatureService) {
        return new InternalApiAspect(requestSignatureService);
    }

    @Bean
    @ConditionalOnMissingBean(name = "internalRequestSigningCapability")
    @ConditionalOnClass(Capability.class)
    @ConditionalOnProperty(prefix = "atlas.security.internal", name = {"service-name", "current-secret"})
    public Capability internalRequestSigningCapability(
            AtlasSecurityProperties securityProperties,
            RequestSignatureService requestSignatureService
    ) {
        return new InternalRequestSigningCapability(
                securityProperties,
                requestSignatureService
        );
    }
}
