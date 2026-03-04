package com.bubua12.atlas.common.mybatis.config;

import com.bubua12.atlas.common.mybatis.handler.AutoFillHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 *
 *
 * @author bubua12
 * @since 2026/3/4 15:08
 */
@AutoConfiguration
@Import(MybatisPlusConfig.class)
public class AtlasDataBaseAutoConfiguration {

    @Bean
    public AutoFillHandler autoFillHandler() {
        return new AutoFillHandler();
    }
}
