package com.bubua12.atlas.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.auth.entity.SysLoginFailRecord;
import com.bubua12.atlas.auth.mapper.SysLoginFailRecordMapper;
import com.bubua12.atlas.auth.service.LoginFailRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录失败记录服务实现
 */
@Service
@RequiredArgsConstructor
public class LoginFailRecordServiceImpl implements LoginFailRecordService {

    private final SysLoginFailRecordMapper mapper;

    // TODO: 从系统配置读取
    private static final int IP_FAIL_MAX = 5;
    private static final int ACCOUNT_FAIL_MAX = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    @Override
    public boolean isIpLocked(String ip) {
        return isLocked("IP", ip);
    }

    @Override
    public boolean isAccountLocked(String username) {
        return isLocked("ACCOUNT", username);
    }

    @Override
    public void recordLoginFail(String ip, String username) {
        recordFail("IP", ip, IP_FAIL_MAX);
        recordFail("ACCOUNT", username, ACCOUNT_FAIL_MAX);
    }

    @Override
    public void clearLoginFail(String ip, String username) {
        clearRecord("IP", ip);
        clearRecord("ACCOUNT", username);
    }

    private boolean isLocked(String type, String key) {
        SysLoginFailRecord record = getRecord(type, key);
        if (record == null || record.getLocked() == 0) {
            return false;
        }
        // 检查锁定是否过期
        if (record.getLockTime() != null &&
            record.getLockTime().plusMinutes(LOCK_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
            // 锁定已过期，解锁
            record.setLocked(0);
            record.setFailCount(0);
            mapper.updateById(record);
            return false;
        }
        return true;
    }

    private void recordFail(String type, String key, int maxFail) {
        SysLoginFailRecord record = getRecord(type, key);
        if (record == null) {
            record = new SysLoginFailRecord();
            record.setRecordType(type);
            record.setRecordKey(key);
            record.setFailCount(1);
            record.setLocked(0);
            record.setLastFailTime(LocalDateTime.now());
            mapper.insert(record);
        } else {
            record.setFailCount(record.getFailCount() + 1);
            record.setLastFailTime(LocalDateTime.now());
            if (record.getFailCount() >= maxFail) {
                record.setLocked(1);
                record.setLockTime(LocalDateTime.now());
            }
            mapper.updateById(record);
        }
    }

    private void clearRecord(String type, String key) {
        LambdaQueryWrapper<SysLoginFailRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysLoginFailRecord::getRecordType, type)
               .eq(SysLoginFailRecord::getRecordKey, key);
        mapper.delete(wrapper);
    }

    private SysLoginFailRecord getRecord(String type, String key) {
        LambdaQueryWrapper<SysLoginFailRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysLoginFailRecord::getRecordType, type)
               .eq(SysLoginFailRecord::getRecordKey, key);
        return mapper.selectOne(wrapper);
    }
}
