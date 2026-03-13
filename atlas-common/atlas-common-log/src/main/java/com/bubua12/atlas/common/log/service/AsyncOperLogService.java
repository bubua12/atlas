package com.bubua12.atlas.common.log.service;

import com.bubua12.atlas.common.log.entity.SysOperLog;
import com.bubua12.atlas.common.log.mapper.SysOperLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步操作日志服务
 */
@Service
@Slf4j
public class AsyncOperLogService {

    @Resource
    private SysOperLogMapper operLogMapper;

    @Async("atlasLogTaskExecutor")
    public void saveLog(SysOperLog operLog) {
        try {
            operLogMapper.insert(operLog);
            log.debug("[atlas-log-thread: {}] 保存操作日志成功 - title: {}, 耗时: {}ms",
                    Thread.currentThread().getName(), operLog.getTitle(), operLog.getCostTime());
        } catch (Exception e) {
            log.debug("[atlas-log-thread: {}] 保存操作日志失败 - title: {}, error: {}",
                    Thread.currentThread().getName(), operLog.getTitle(), e.getMessage(), e);
        }
    }
}
