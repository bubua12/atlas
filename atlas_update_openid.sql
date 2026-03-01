-- =============================================
-- Atlas 数据库更新脚本
-- 增加 open_id 字段
-- =============================================

USE `atlas`;

-- 1. 用户表增加 open_id 字段
ALTER TABLE `sys_user`
    ADD COLUMN `open_id` VARCHAR(64) DEFAULT NULL COMMENT '微信OpenID' AFTER `user_id`;

-- 2. 增加唯一索引（可选，根据业务需求，一个微信号通常只能绑定一个账号）
ALTER TABLE `sys_user`
    ADD UNIQUE KEY `uk_open_id` (`open_id`);
