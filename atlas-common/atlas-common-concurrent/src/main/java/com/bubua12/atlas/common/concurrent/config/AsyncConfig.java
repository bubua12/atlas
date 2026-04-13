package com.bubua12.atlas.common.concurrent.config;

import com.bubua12.atlas.common.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义线程池配置
 * 
 * 优化点：
 * 1. 配置 TaskDecorator 传递 ThreadLocal 上下文到异步线程
 * 2. 确保异步任务可以访问用户信息
 *
 * @author bubua12
 * @since 2026/3/12 19:04
 */
@Slf4j
@Configuration
@EnableAsync // 开启异步支持
public class AsyncConfig {

    /**
     * 日志异步任务执行器
     * 配置 TaskDecorator 传递用户上下文到异步线程
     */
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
        
        // 配置 TaskDecorator 传递 ThreadLocal 到异步线程
        executor.setTaskDecorator(runnable -> {
            // 捕获当前线程的用户上下文
            SecurityContextHolder.UserContext context = SecurityContextHolder.getUserContext();
            return () -> {
                try {
                    // 在异步线程中设置用户上下文
                    if (context != null) {
                        SecurityContextHolder.setUserContext(
                            context.getUserId(), 
                            context.getUsername(), 
                            context.getToken()
                        );
                        log.debug("异步线程设置用户上下文: userId={}", context.getUserId());
                    }
                    runnable.run();
                } finally {
                    // 清理异步线程的 ThreadLocal
                    SecurityContextHolder.clear();
                    log.debug("异步线程清理用户上下文");
                }
            };
        });
        
        executor.initialize();
        return executor;
    }
}
