package com.bubua12.atlas.common.log.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 *
 * @author bubua12
 * @since 2026/3/12 19:08
 */

@Service
@Slf4j
public class AsyncOperLogService {

    // 指定使用自己配置的线程池 todo 这里专门业务专门线程池是不是更好
    @Async("atlasLogTaskExecutor")
    public void saveLog(String title, String method, long elapsed) {
        // 模拟耗时操作，例如入库 fixme 后面去除，这里只是模拟
        try {
            Thread.sleep(100);
            log.info("[Async-Thread: {}] 保存日志 - title: {}, 耗时: {}ms",
                    Thread.currentThread().getName(), title, elapsed);
        } catch (InterruptedException e) {
            // fixme 更优雅的错误日志打印方法的抽取
            e.printStackTrace();
        }
    }
}
