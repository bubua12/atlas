package com.bubua12.atlas.common.concurrent.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池构建器
 * 1. 统一构建标准线程池
 * 2. 自动集成上下文传递 (AtlasThreadPoolTaskExecutor)
 * 3. 自动打印线程池参数日志
 * 4. (未来可扩展) 自动注册到 Prometheus 监控
 */
@Slf4j
public class ThreadPoolBuilder {

    /**
     * 构建一个增强型线程池
     *
     * @param threadNamePrefix 线程名前缀 (建议加上业务标识，如 "order-async-")
     * @param corePoolSize     核心线程数
     * @param maxPoolSize      最大线程数
     * @param queueCapacity    队列容量
     * @return 增强后的 ThreadPoolTaskExecutor
     */
    public static ThreadPoolTaskExecutor build(String threadNamePrefix, int corePoolSize, int maxPoolSize, int queueCapacity) {
        // 使用我们需要上下文增强的子类
        AtlasThreadPoolTaskExecutor executor = new AtlasThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);

        // 拒绝策略：默认使用 CallerRunsPolicy (主线程自己跑)，防止任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 允许核心线程超时销毁 (可选，节省资源)
        executor.setAllowCoreThreadTimeOut(true);
        // 线程存活时间
        executor.setKeepAliveSeconds(60);

        // 初始化
        executor.initialize();

        // 打印启动日志
        log.info("Initialized ThreadPool [{}]: core={}, max={}, queue={}", 
                threadNamePrefix, corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }
}
