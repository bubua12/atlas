package com.bubua12.atlas.common.security.config;

import com.bubua12.atlas.common.redis.service.RedisService;
import com.bubua12.atlas.common.security.aspect.PreAuthorizeAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PreAuthorizeAspect 配置类
 * 只在 RedisService 存在时加载
 */
@Configuration
@ConditionalOnClass(RedisService.class)
public class PreAuthorizeAspectConfiguration {

    @Bean
    public PreAuthorizeAspect preAuthorizeAspect(RedisService redisService) {
        return new PreAuthorizeAspect(redisService);
    }
}
