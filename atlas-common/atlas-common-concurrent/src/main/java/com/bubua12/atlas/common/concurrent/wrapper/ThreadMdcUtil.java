package com.bubua12.atlas.common.concurrent.wrapper;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 装饰器模式：增强 Runnable 和 Callable
 * 1. 支持 ThreadLocal 上下文传递（基于 Alibaba TTL）
 * 2. 支持 MDC 日志追踪 ID 传递
 *
 * 解决父子线程上下文丢失的核心类
 */
public class ThreadMdcUtil {

    /**
     * 包装 Runnable，实现上下文传递
     */
    public static Runnable wrap(Runnable runnable, Map<String, String> context) {
        return new TtlMdcRunnable(runnable, context);
    }

    /**
     * 包装 Callable，实现上下文传递
     */
    public static <T> Callable<T> wrap(Callable<T> callable, Map<String, String> context) {
        return new TtlMdcCallable<>(callable, context);
    }

    /**
     * 内部类：增强版 Runnable
     */
    public static class TtlMdcRunnable implements Runnable {
        private final Runnable runnable;
        private final Map<String, String> context;

        public TtlMdcRunnable(Runnable runnable, Map<String, String> context) {
            this.runnable = runnable;
            // 捕获父线程的 MDC 上下文
            this.context = context != null ? context : MDC.getCopyOfContextMap();
        }

        @Override
        public void run() {
            // 1. 备份子线程原本的 MDC（防止污染）
            Map<String, String> backup = MDC.getCopyOfContextMap();
            try {
                // 2. 将父线程的 MDC 设置到子线程
                if (context != null) {
                    MDC.setContextMap(context);
                }
                // 3. 执行业务逻辑
                runnable.run();
            } finally {
                // 4. 恢复子线程原本的 MDC
                if (backup != null) {
                    MDC.setContextMap(backup);
                } else {
                    MDC.clear();
                }
            }
        }
    }

    /**
     * 内部类：增强版 Callable
     */
    public static class TtlMdcCallable<T> implements Callable<T> {
        private final Callable<T> callable;
        private final Map<String, String> context;

        public TtlMdcCallable(Callable<T> callable, Map<String, String> context) {
            this.callable = callable;
            this.context = context != null ? context : MDC.getCopyOfContextMap();
        }

        @Override
        public T call() throws Exception {
            Map<String, String> backup = MDC.getCopyOfContextMap();
            try {
                if (context != null) {
                    MDC.setContextMap(context);
                }
                return callable.call();
            } finally {
                if (backup != null) {
                    MDC.setContextMap(backup);
                } else {
                    MDC.clear();
                }
            }
        }
    }
}
