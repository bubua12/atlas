package com.bubua12.atlas.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.bubua12.atlas.common.mybatis.handler.AutoFillHandler;
import com.bubua12.atlas.common.mybatis.handler.DataScopeHandler;
import com.bubua12.atlas.common.mybatis.interceptor.DataScopeInnerInterceptor;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Atlas自动配置
 * 注册分页插件，支持 MySQL 分页查询。
 *
 * @author bubua12
 * @since 2026/3/4 15:08
 */
@AutoConfiguration
public class AtlasDataBaseAutoConfiguration {

    @Bean
    public AutoFillHandler autoFillHandler() {
        return new AutoFillHandler();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 数据权限拦截器（必须在分页拦截器之前）
        interceptor.addInnerInterceptor(new DataScopeInnerInterceptor(dataScopeHandler()));
        // 分页拦截器 fixme 这里分页拦截器必须要在最后 因为分页插件需要基于最终的、完整的 SQL 语句来拼接 LIMIT 条件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 凡是依赖了 atlas-common-database 的模块，MyBatis-Plus 默认都走 Slf4jImpl
     * 也就是“把 MyBatis 日志接到统一的 SLF4J/Logback 体系里”，不是“无条件把 SQL 全打印出来”。
     */
    @Bean
    public MybatisPlusPropertiesCustomizer mybatisPlusPropertiesCustomizer() {
        return properties -> {
            MybatisPlusProperties.CoreConfiguration configuration = properties.getConfiguration();
            if (configuration == null) {
                configuration = new MybatisPlusProperties.CoreConfiguration();
                properties.setConfiguration(configuration);
            }
            if (configuration.getLogImpl() == null) {
                configuration.setLogImpl(Slf4jImpl.class);
            }
        };
    }

    @Bean
    public DataScopeHandler dataScopeHandler() {
        return new DataScopeHandler();
    }
}
