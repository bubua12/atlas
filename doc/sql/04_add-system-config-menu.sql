-- =============================================
-- 添加系统设置菜单
-- 执行时间: 2026-03-05
-- =============================================

-- 添加系统设置菜单（二级菜单）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `sort`, `path`, `component`, `menu_type`, `perms`, `icon`,
                        `visible`, `status`, `create_by`, `create_time`)
VALUES (15, '系统设置', 1, 6, 'config', 'system/config/index', 'C', 'system:config:list', 'setting', 0, 0, 'admin', NOW());

-- 添加系统设置按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `sort`, `path`, `component`, `menu_type`, `perms`, `icon`,
                        `visible`, `status`, `create_by`, `create_time`)
VALUES (150, '配置查询', 15, 1, '', NULL, 'F', 'system:config:query', '#', 0, 0, 'admin', NOW()),
       (151, '配置修改', 15, 2, '', NULL, 'F', 'system:config:edit', '#', 0, 0, 'admin', NOW());

-- 为超级管理员角色分配系统设置菜单权限（假设角色ID为1）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 15), (1, 150), (1, 151);
