package com.bubua12.atlas.common.log.config;

import com.bubua12.atlas.common.log.aspect.OperLogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 *
 *
 * @author bubua12
 * @since 2026/3/4 15:11
 */
@AutoConfiguration
@Import({OperLogAspect.class})
public class AtlasLogAutoConfiguration {

}
