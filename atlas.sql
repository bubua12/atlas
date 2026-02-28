-- =============================================
-- Atlas 企业级微服务开发平台 - 数据库初始化脚本
-- 数据库: MySQL 8.x
-- 字符集: utf8mb4
-- =============================================

-- 创建数据库
CREATE
DATABASE IF NOT EXISTS `atlas` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE
`atlas`;

-- =============================================
-- 1. 部门表
-- =============================================
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`
(
    `dept_id`     BIGINT      NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id`   BIGINT      NOT NULL DEFAULT 0 COMMENT '父部门ID（0表示顶级部门）',
    `dept_name`   VARCHAR(64) NOT NULL DEFAULT '' COMMENT '部门名称',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '显示排序',
    `leader`      VARCHAR(64)          DEFAULT NULL COMMENT '负责人',
    `phone`       VARCHAR(20)          DEFAULT NULL COMMENT '联系电话',
    `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    `create_by`   VARCHAR(64)          DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME             DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)          DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME             DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';

-- =============================================
-- 2. 用户表
-- =============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `user_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户账号',
    `nickname`    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '用户昵称',
    `password`    VARCHAR(128) NOT NULL DEFAULT '' COMMENT '密码（BCrypt加密）',
    `email`       VARCHAR(128)          DEFAULT '' COMMENT '邮箱',
    `phone`       VARCHAR(20)           DEFAULT '' COMMENT '手机号码',
    `sex`         TINYINT      NOT NULL DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
    `avatar`      VARCHAR(256)          DEFAULT '' COMMENT '头像地址',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `dept_id`     BIGINT                DEFAULT NULL COMMENT '所属部门ID',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    `create_by`   VARCHAR(64)           DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)           DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- =============================================
-- 3. 角色表
-- =============================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `role_id`     BIGINT      NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(64) NOT NULL COMMENT '角色名称',
    `role_key`    VARCHAR(64) NOT NULL COMMENT '角色权限标识（如 admin、editor）',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '显示排序',
    `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    `create_by`   VARCHAR(64)          DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME             DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)          DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME             DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`role_id`),
    UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- =============================================
-- 4. 菜单表
-- =============================================
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`
(
    `menu_id`     BIGINT      NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name`   VARCHAR(64) NOT NULL COMMENT '菜单名称',
    `parent_id`   BIGINT      NOT NULL DEFAULT 0 COMMENT '父菜单ID（0表示顶级菜单）',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '显示排序',
    `path`        VARCHAR(256)         DEFAULT '' COMMENT '路由地址',
    `component`   VARCHAR(256)         DEFAULT NULL COMMENT '组件路径',
    `menu_type`   CHAR(1)     NOT NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    `perms`       VARCHAR(128)         DEFAULT NULL COMMENT '权限标识（如 system:user:list）',
    `icon`        VARCHAR(128)         DEFAULT '#' COMMENT '菜单图标',
    `visible`     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否可见（0显示 1隐藏）',
    `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `create_by`   VARCHAR(64)          DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME             DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)          DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME             DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';

-- =============================================
-- 5. 字典类型表
-- =============================================
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`
(
    `dict_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    `dict_name`   VARCHAR(128) NOT NULL DEFAULT '' COMMENT '字典名称',
    `dict_type`   VARCHAR(128) NOT NULL DEFAULT '' COMMENT '字典类型（唯一标识，如 sys_user_sex）',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `create_by`   VARCHAR(64)           DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)           DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`dict_id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型表';

-- =============================================
-- 6. 字典数据表
-- =============================================
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`
(
    `dict_code`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字典编码',
    `dict_sort`   INT          NOT NULL DEFAULT 0 COMMENT '字典排序',
    `dict_label`  VARCHAR(128) NOT NULL DEFAULT '' COMMENT '字典标签',
    `dict_value`  VARCHAR(128) NOT NULL DEFAULT '' COMMENT '字典键值',
    `dict_type`   VARCHAR(128) NOT NULL DEFAULT '' COMMENT '所属字典类型',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `create_by`   VARCHAR(64)           DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64)           DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`dict_code`),
    KEY           `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典数据表';

-- =============================================
-- 7. 用户与角色关联表（多对多）
-- =============================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户与角色关联表';

-- =============================================
-- 8. 角色与菜单关联表（多对多）
-- =============================================
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`
(
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色与菜单关联表';

-- =============================================
-- 初始化数据
-- =============================================

-- ----- 部门 -----
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `dept_name`, `sort`, `leader`, `phone`, `status`, `create_by`,
                        `create_time`)
VALUES (1, 0, 'Atlas科技', 0, 'admin', '15888888888', 0, 'admin', NOW()),
       (2, 1, '研发部', 1, NULL, NULL, 0, 'admin', NOW()),
       (3, 1, '产品部', 2, NULL, NULL, 0, 'admin', NOW()),
       (4, 1, '运维部', 3, NULL, NULL, 0, 'admin', NOW());

-- ----- 角色 -----
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `sort`, `status`, `create_by`, `create_time`)
VALUES (1, '超级管理员', 'admin', 1, 0, 'admin', NOW()),
       (2, '普通角色', 'common', 2, 0, 'admin', NOW());

-- ----- 用户（密码明文: admin123，生产环境请使用BCrypt加密） -----
INSERT INTO `sys_user` (`user_id`, `username`, `nickname`, `password`, `email`, `phone`, `sex`, `avatar`, `status`,
                        `dept_id`, `create_by`, `create_time`)
VALUES (1, 'admin', '超级管理员', 'admin123', 'admin@atlas.com', '15888888888', 1, '', 0, 1, 'admin', NOW()),
       (2, 'atlas', '普通用户', 'admin123', 'atlas@atlas.com', '15888888889', 1, '', 0, 2, 'admin', NOW());

-- ----- 菜单（一级目录） -----
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `sort`, `path`, `component`, `menu_type`, `perms`, `icon`,
                        `visible`, `status`, `create_by`, `create_time`)
VALUES (1, '系统管理', 0, 1, 'system', NULL, 'M', NULL, 'system', 0, 0, 'admin', NOW()),
       (2, '系统监控', 0, 2, 'monitor', NULL, 'M', NULL, 'monitor', 0, 0, 'admin', NOW()),
       (3, '基础设施', 0, 3, 'infra', NULL, 'M', NULL, 'infra', 0, 0, 'admin', NOW());

-- ----- 菜单（系统管理 - 二级菜单） -----
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `sort`, `path`, `component`, `menu_type`, `perms`, `icon`,
                        `visible`, `status`, `create_by`, `create_time`)
VALUES (10, '用户管理', 1, 1, 'user', 'system/user/index', 'C', 'system:user:list', 'user', 0, 0, 'admin', NOW()),
       (11, '角色管理', 1, 2, 'role', 'system/role/index', 'C', 'system:role:list', 'peoples', 0, 0, 'admin', NOW()),
       (12, '菜单管理', 1, 3, 'menu', 'system/menu/index', 'C', 'system:menu:list', 'tree-table', 0, 0, 'admin', NOW()),
       (13, '部门管理', 1, 4, 'dept', 'system/dept/index', 'C', 'system:dept:list', 'tree', 0, 0, 'admin', NOW()),
       (14, '字典管理', 1, 5, 'dict', 'system/dict/index', 'C', 'system:dict:list', 'dict', 0, 0, 'admin', NOW());

-- ----- 菜单（用户管理 - 按钮权限） -----
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `sort`, `path`, `component`, `menu_type`, `perms`, `icon`,
                        `visible`, `status`, `create_by`, `create_time`)
VALUES (100, '用户查询', 10, 1, '', NULL, 'F', 'system:user:query', '#', 0, 0, 'admin', NOW()),
       (101, '用户新增', 10, 2, '', NULL, 'F', 'system:user:add', '#', 0, 0, 'admin', NOW()),
       (102, '用户修改', 10, 3, '', NULL, 'F', 'system:user:edit', '#', 0, 0, 'admin', NOW()),
       (103, '用户删除', 10, 4, '', NULL, 'F', 'system:user:remove', '#', 0, 0, 'admin', NOW());

-- ----- 菜单（系统监控 - 二级菜单） -----
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `sort`, `path`, `component`, `menu_type`, `perms`, `icon`,
                        `visible`, `status`, `create_by`, `create_time`)
VALUES (20, '在线用户', 2, 1, 'online', 'monitor/online/index', 'C', 'monitor:online:list', 'online', 0, 0, 'admin',
        NOW()),
       (21, '服务监控', 2, 2, 'server', 'monitor/server/index', 'C', 'monitor:server:list', 'server', 0, 0, 'admin',
        NOW());

-- ----- 菜单（基础设施 - 二级菜单） -----
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `sort`, `path`, `component`, `menu_type`, `perms`, `icon`,
                        `visible`, `status`, `create_by`, `create_time`)
VALUES (30, '文件管理', 3, 1, 'file', 'infra/file/index', 'C', 'infra:file:list', 'upload', 0, 0, 'admin', NOW()),
       (31, '代码生成', 3, 2, 'codegen', 'infra/codegen/index', 'C', 'infra:codegen:list', 'code', 0, 0, 'admin',
        NOW());

-- ----- 字典类型 -----
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`)
VALUES (1, '用户性别', 'sys_user_sex', 0, 'admin', NOW()),
       (2, '系统状态', 'sys_common_status', 0, 'admin', NOW()),
       (3, '菜单类型', 'sys_menu_type', 0, 'admin', NOW()),
       (4, '是否可见', 'sys_show_hide', 0, 'admin', NOW());

-- ----- 字典数据 -----
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `create_by`,
                             `create_time`)
VALUES (1, 1, '未知', '0', 'sys_user_sex', 0, 'admin', NOW()),
       (2, 2, '男', '1', 'sys_user_sex', 0, 'admin', NOW()),
       (3, 3, '女', '2', 'sys_user_sex', 0, 'admin', NOW()),
       (4, 1, '正常', '0', 'sys_common_status', 0, 'admin', NOW()),
       (5, 2, '停用', '1', 'sys_common_status', 0, 'admin', NOW()),
       (6, 1, '目录', 'M', 'sys_menu_type', 0, 'admin', NOW()),
       (7, 2, '菜单', 'C', 'sys_menu_type', 0, 'admin', NOW()),
       (8, 3, '按钮', 'F', 'sys_menu_type', 0, 'admin', NOW()),
       (9, 1, '显示', '0', 'sys_show_hide', 0, 'admin', NOW()),
       (10, 2, '隐藏', '1', 'sys_show_hide', 0, 'admin', NOW());

-- ----- 用户与角色关联 -----
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
VALUES (1, 1),
       (2, 2);

-- ----- 角色与菜单关联（超级管理员拥有所有菜单权限） -----
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 10),
       (1, 11),
       (1, 12),
       (1, 13),
       (1, 14),
       (1, 20),
       (1, 21),
       (1, 30),
       (1, 31),
       (1, 100),
       (1, 101),
       (1, 102),
       (1, 103);

-- ----- 普通角色仅拥有查看权限 -----
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES (2, 1),
       (2, 10),
       (2, 100);

