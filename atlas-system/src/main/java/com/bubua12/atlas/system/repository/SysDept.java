package com.bubua12.atlas.system.repository;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门
 */
@Data
@TableName("sys_dept")
public class SysDept {

    /**
     * 部门ID
     */
    @TableId
    private Long deptId;

    /**
     * 父部门ID（0表示顶级部门）
     */
    private Long parentId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 显示排序
     */
    private Integer sort;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

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

    @TableField(exist = false)
    private List<SysDept> children;
}
