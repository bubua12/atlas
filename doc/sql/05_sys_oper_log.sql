-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_oper_log`
(
    `oper_id`        BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志主键',
    `title`          VARCHAR(50)   DEFAULT '' COMMENT '操作标题',
    `business_type`  VARCHAR(20)   DEFAULT '' COMMENT '业务类型',
    `method`         VARCHAR(200)  DEFAULT '' COMMENT '方法名称',
    `request_method` VARCHAR(10)   DEFAULT '' COMMENT '请求方式',
    `oper_name`      VARCHAR(50)   DEFAULT '' COMMENT '操作人员',
    `oper_ip`        VARCHAR(128)  DEFAULT '' COMMENT '操作IP',
    `oper_param`     VARCHAR(2000) DEFAULT '' COMMENT '请求参数',
    `json_result`    VARCHAR(2000) DEFAULT '' COMMENT '返回结果',
    `status`         INT           DEFAULT 0 COMMENT '操作状态（0成功 1失败）',
    `error_msg`      VARCHAR(2000) DEFAULT '' COMMENT '错误消息',
    `oper_time`      DATETIME      DEFAULT NULL COMMENT '操作时间',
    `cost_time`      BIGINT        DEFAULT 0 COMMENT '执行耗时（毫秒）',
    PRIMARY KEY (`oper_id`),
    KEY `idx_oper_time` (`oper_time`),
    KEY `idx_oper_name` (`oper_name`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4 COMMENT ='操作日志表';

