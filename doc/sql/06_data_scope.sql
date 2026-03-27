-- 数据权限功能数据库变更脚本
-- 执行日期: 2026-03-27

-- 1. 扩展 sys_role 表，添加 data_scope 字段
ALTER TABLE sys_role ADD COLUMN data_scope TINYINT DEFAULT 1 COMMENT '数据权限：1=全部 2=本部门及下级 3=仅本部门 4=仅本人 5=自定义';

-- 2. 创建角色-部门关联表（用于自定义部门权限）
CREATE TABLE IF NOT EXISTS sys_role_dept (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  dept_id BIGINT NOT NULL COMMENT '部门ID',
  PRIMARY KEY (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-部门关联表';
