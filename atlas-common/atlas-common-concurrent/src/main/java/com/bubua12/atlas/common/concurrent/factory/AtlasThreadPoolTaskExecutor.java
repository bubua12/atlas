package com.bubua12.atlas.common.concurrent.factory;

import com.bubua12.atlas.common.concurrent.wrapper.ThreadMdcUtil;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 增强型 Spring 线程池
 * 1. 自动捕获父线程的 MDC 上下文
 * 2. 自动传递给子线程
 */
public class AtlasThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    @Override
    public void execute(Runnable task) {
        // 1. 获取当前（父）线程的 MDC 上下文
        Map<String, String> context = MDC.getCopyOfContextMap();
        // 2. 包装任务，把上下文带过去
        super.execute(new ThreadMdcUtil.TtlMdcRunnable(task, context));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return super.submit(new ThreadMdcUtil.TtlMdcCallable<>(task, context));
    }

    @Override
    public Future<?> submit(Runnable task) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return super.submit(new ThreadMdcUtil.TtlMdcRunnable(task, context));
    }
}
