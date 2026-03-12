package com.bubua12.atlas.common.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 *
 *
 * @author bubua12
 * @since 2026/3/12 19:17
 */
@Import({AsyncConfig.class})
@AutoConfiguration
public class AtlasCoreAutoConfiguration {

}
