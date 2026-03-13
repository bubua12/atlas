package com.bubua12.atlas.common.concurrent.config;

import com.bubua12.atlas.common.concurrent.factory.ThreadPoolBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 全局异步配置
 * 1. 开启 @EnableAsync 支持
 * 2. 提供默认的线程池 (atlasAsyncExecutor)
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 定义一个默认的全局业务线程池
     * 业务模块可直接使用 @Async("atlasAsyncExecutor")
     */
    @Bean("atlasAsyncExecutor")
    public Executor atlasAsyncExecutor() {
        // CPU 核心数
        int cpuCores = Runtime.getRuntime().availableProcessors();
        
        // 核心线程数：CPU 密集型通常设为 CPU 核数 + 1
        int corePoolSize = cpuCores + 1;
        // 最大线程数：根据负载预估
        int maxPoolSize = cpuCores * 2;
        // 队列容量：根据内存预估
        int queueCapacity = 200;

        return ThreadPoolBuilder.build("atlas-async-", corePoolSize, maxPoolSize, queueCapacity);
    }
}
