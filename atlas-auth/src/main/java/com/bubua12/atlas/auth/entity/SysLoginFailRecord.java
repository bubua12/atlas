package com.bubua12.atlas.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录失败记录实体
 */
@Data
@TableName("sys_login_fail_record")
public class SysLoginFailRecord {

    @TableId
    private Long id;

    /**
     * 记录类型：IP/ACCOUNT
     */
    private String recordType;

    /**
     * IP地址或用户名
     */
    private String recordKey;

    /**
     * 失败次数
     */
    private Integer failCount;

    /**
     * 是否锁定：0否 1是
     */
    private Integer locked;

    /**
     * 锁定时间
     */
    private LocalDateTime lockTime;

    /**
     * 最后失败时间
     */
    private LocalDateTime lastFailTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
