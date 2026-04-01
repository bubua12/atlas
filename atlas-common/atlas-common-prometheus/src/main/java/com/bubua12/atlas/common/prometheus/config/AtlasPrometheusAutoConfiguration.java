package com.bubua12.atlas.common.prometheus.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 *
 *
 * @author bubua12
 * @since 2026/04/01 23:47
 */
@AutoConfiguration
public class AtlasPrometheusAutoConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }
}
