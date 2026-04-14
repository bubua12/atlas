package com.bubua12.atlas.common.security.config;

import com.bubua12.atlas.common.security.aspect.InternalApiAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 仅在 Servlet Web 应用中装配内部接口切面，避免 Reactive 应用加载 servlet API。
 */
@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
public class InternalApiAspectConfiguration {

    @Bean
    public InternalApiAspect internalApiAspect() {
        return new InternalApiAspect();
    }
}
