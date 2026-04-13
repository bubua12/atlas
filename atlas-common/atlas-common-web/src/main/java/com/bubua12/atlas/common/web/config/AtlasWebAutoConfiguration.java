package com.bubua12.atlas.common.web.config;

import com.bubua12.atlas.common.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.context.annotation.Import;

/**
 * Atlas Web模块自动配置类
 *
 * @author bubua12
 * @since 2026/3/4 15:01
 */
@AutoConfiguration
@Import({WebMvcConfig.class, GlobalExceptionHandler.class, UserConfigurations.class, FeignServiceNameInterceptor.class})
public class AtlasWebAutoConfiguration {
}
