package com.bubua12.atlas.common.opentelemetry.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 *
 *
 * @author bubua12
 * @since 2026/3/26 10:38
 */
@Component
public class OpenTelemetryEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Resource resource = new ClassPathResource("opentelemetry-starter.yml");
        if (resource.exists()) {
            try {
                List<PropertySource<?>> propertySources = loader.load("opentelemetry-starter", resource);
                for (PropertySource<?> source : propertySources) {
                    // 使用 addLast 确保优先级最低，这样业务方项目中的配置可以覆盖这些默认值
                    environment.getPropertySources().addLast(source);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load opentelemetry-starter.yml", e);
            }
        }
    }
}
