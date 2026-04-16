package com.bubua12.atlas.common.security.config;

import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.aspect.PreAuthorizeAspect;
import com.bubua12.atlas.common.security.utils.JwtUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * `PreAuthorizeAspect` 配置。
 *
 * <p>单独拆一个配置类，主要是为了让权限切面按需装配，
 * 同时避免 security 模块自动配置里堆太多与 MVC/AOP 相关的细节。
 */
@Configuration
@ConditionalOnClass(RedisService.class)
public class PreAuthorizeAspectConfiguration {

    @Bean
    public PreAuthorizeAspect preAuthorizeAspect(RedisService redisService, JwtUtils jwtUtils) {
        return new PreAuthorizeAspect(redisService, jwtUtils);
    }
}
