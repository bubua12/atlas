package com.bubua12.atlas.common.log.config;

import com.bubua12.atlas.common.log.aspect.OperLogAspect;
import com.bubua12.atlas.common.log.service.AsyncOperLogService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 操作日志自动配置
 */
@AutoConfiguration
@Import({OperLogAspect.class, AsyncOperLogService.class})
@MapperScan("com.bubua12.atlas.common.log.mapper") // 对于mapper接口，不能使用 @Import注解了
public class AtlasLogAutoConfiguration {

}
