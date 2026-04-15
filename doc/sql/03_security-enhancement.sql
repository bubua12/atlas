-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config
(
    config_id
    BIGINT
    PRIMARY
    KEY
    AUTO_INCREMENT
    COMMENT
    '配置ID',
    config_key
    VARCHAR
(
    100
) UNIQUE NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR
(
    50
) COMMENT '配置类型：PASSWORD/ACCOUNT/WATERMARK',
    description VARCHAR
(
    500
) COMMENT '配置描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 登录失败记录表
-- CREATE TABLE IF NOT EXISTS sys_login_fail_record (
--     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
--     record_type VARCHAR(20) NOT NULL COMMENT '记录类型：IP/ACCOUNT',
--     record_key VARCHAR(100) NOT NULL COMMENT 'IP地址或用户名',
--     fail_count INT DEFAULT 0 COMMENT '失败次数',
--     locked TINYINT DEFAULT 0 COMMENT '是否锁定：0否 1是',
--     lock_time DATETIME COMMENT '锁定时间',
--     last_fail_time DATETIME COMMENT '最后失败时间',
--     create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
--     update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
--     UNIQUE KEY uk_type_key (record_type, record_key)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录失败记录表';

-- 初始化默认配置
INSERT INTO sys_config (config_key, config_value, config_type, description)
VALUES ('password.min.length', '8', 'PASSWORD', '密码最小长度'),
       ('password.require.uppercase', 'true', 'PASSWORD', '密码需要大写字母'),
       ('password.require.lowercase', 'true', 'PASSWORD', '密码需要小写字母'),
       ('password.require.number', 'true', 'PASSWORD', '密码需要数字'),
       ('password.require.special', 'false', 'PASSWORD', '密码需要特殊字符'),
       ('account.online.threshold', '1', 'ACCOUNT', '同一用户在线阈值'),
       ('account.ip.fail.max', '5', 'ACCOUNT', 'IP登录失败最大次数'),
       ('account.account.fail.max', '5', 'ACCOUNT', '账号登录失败最大次数'),
       ('account.lock.duration', '30', 'ACCOUNT', '锁定时长（分钟）'),
       ('watermark.enabled', 'false', 'WATERMARK', '是否启用水印'),
       ('watermark.text', 'Atlas System', 'WATERMARK', '水印文本'),
       ('watermark.opacity', '0.1', 'WATERMARK', '水印透明度');
