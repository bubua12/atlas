package com.bubua12.atlas.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.bubua12.atlas.common.mybatis.handler.DataScopeHandler;
import com.bubua12.atlas.common.mybatis.interceptor.DataScopeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 注册分页插件，支持 MySQL 分页查询。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public DataScopeHandler dataScopeHandler() {
        return new DataScopeHandler();
    }

    @Bean
    public DataScopeInterceptor dataScopeInterceptor(DataScopeHandler dataScopeHandler) {
        return new DataScopeInterceptor(dataScopeHandler);
    }
}
