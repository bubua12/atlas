package com.bubua12.atlas.system.repository;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色
 */
@Data
@TableName("sys_role")
public class SysRole {

    /**
     * 角色ID
     */
    @TableId
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限标识（如 admin、editor）
     */
    private String roleKey;

    /**
     * 显示排序
     */
    private Integer sort;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 数据权限范围（1=全部 2=本部门及下级 3=仅本部门 4=仅本人 5=自定义）
     */
    private Integer dataScope;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private Integer deleted;
}
