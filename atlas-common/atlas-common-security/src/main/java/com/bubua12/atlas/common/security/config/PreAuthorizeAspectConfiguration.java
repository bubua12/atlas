package com.bubua12.atlas.common.security.config;

import com.bubua12.atlas.common.security.aspect.InternalApiAspect;
import com.bubua12.atlas.common.security.aspect.PreAuthorizeAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PreAuthorizeAspectConfiguration {

    @Bean
    public PreAuthorizeAspect preAuthorizeAspect() {
        return new PreAuthorizeAspect();
    }

    @Bean
    public InternalApiAspect internalApiAspect() {
        return new InternalApiAspect();
    }
}
