package com.bubua12.atlas.common.opentelemetry.config;

import com.bubua12.atlas.common.opentelemetry.aspect.CommonResultTraceAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * OpenTelemetry可观测性自动配置类
 *
 * @author bubua12
 * @since 2026/3/26 10:33
 */
@AutoConfiguration
public class AtlasOpenTelemetryAutoConfiguration {

    @Bean
    public CommonResultTraceAspect commonResultTraceAspect() {
        return new CommonResultTraceAspect();
    }
}
