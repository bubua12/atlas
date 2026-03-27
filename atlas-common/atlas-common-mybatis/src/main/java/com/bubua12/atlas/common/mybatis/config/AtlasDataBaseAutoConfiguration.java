package com.bubua12.atlas.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.bubua12.atlas.common.mybatis.handler.AutoFillHandler;
import com.bubua12.atlas.common.mybatis.handler.DataScopeHandler;
import com.bubua12.atlas.common.mybatis.interceptor.DataScopeInnerInterceptor;
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
        // 分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public DataScopeHandler dataScopeHandler() {
        return new DataScopeHandler();
    }
}
