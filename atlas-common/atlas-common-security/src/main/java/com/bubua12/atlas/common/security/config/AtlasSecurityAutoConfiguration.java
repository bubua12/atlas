package com.bubua12.atlas.common.security.config;

import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.aspect.InternalApiAspect;
import com.bubua12.atlas.common.security.service.RequestSignatureService;
import com.bubua12.atlas.common.security.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Atlas 安全模块自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(AtlasSecurityProperties.class)
@Import(PreAuthorizeAspectConfiguration.class)
public class AtlasSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestSignatureService requestSignatureService(
            RedisService redisService,
            AtlasSecurityProperties securityProperties,
            ObjectProvider<ObjectMapper> objectMapperProvider
    ) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        return new RequestSignatureService(redisService, objectMapper, securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalApiAspect internalApiAspect(RequestSignatureService requestSignatureService) {
        return new InternalApiAspect(requestSignatureService);
    }
}
