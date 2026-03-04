package com.bubua12.atlas.common.security.config;

import com.bubua12.atlas.common.security.aspect.PreAuthorizeAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Atlas 认证模块自动配置类
 *
 * @author bubua12
 * @since 2026/3/4 15:04
 */
@AutoConfiguration
@Import(PreAuthorizeAspect.class)
public class AtlasSecurityAutoConfiguration {

}
