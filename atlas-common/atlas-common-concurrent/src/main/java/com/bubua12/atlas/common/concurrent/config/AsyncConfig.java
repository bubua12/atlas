package com.bubua12.atlas.common.concurrent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义线程池
 *
 * @author bubua12
 * @since 2026/3/12 19:04
 */
@Configuration
@EnableAsync // 开启异步支持
public class AsyncConfig {


    // 定义名为 "taskExecutor" 的 Bean，Spring 会自动识别 fixme 是自动识别特定的名称嘛
    @Bean("atlasLogTaskExecutor")
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：建议设为 CPU 核心数 + 1
        executor.setCorePoolSize(10);
        // 最大线程数：建议设为核心数 * 2
        executor.setMaxPoolSize(20);
        // 队列容量：根据业务量设置，避免过大导致内存溢出
        executor.setQueueCapacity(200);
        // 线程名前缀：方便排查日志
        executor.setThreadNamePrefix("atlas-async-log-");
        // 拒绝策略：由调用线程处理（CallerRunsPolicy），保证不丢任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // fixme 上述的初始化步骤，要了解一下
        executor.initialize();

        return executor;
    }
}
