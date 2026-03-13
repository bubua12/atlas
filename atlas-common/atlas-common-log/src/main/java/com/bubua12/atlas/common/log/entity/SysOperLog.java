package com.bubua12.atlas.common.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 * todo 后续优化为可配置持久化终端 mongo、mysql
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    /**
     * 日志主键
     */
    @TableId(type = IdType.AUTO)
    private Long operId;

    /**
     * 操作标题
     */
    private String title;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 方法名称
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作人员
     */
    private String operName;

    /**
     * 操作IP
     */
    private String operIp;

    /**
     * 请求参数
     */
    private String operParam;

    /**
     * 返回结果
     */
    private String jsonResult;

    /**
     * 操作状态（0成功 1失败）
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private LocalDateTime operTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long costTime;
}
