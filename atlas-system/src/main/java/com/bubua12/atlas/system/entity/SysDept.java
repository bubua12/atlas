package com.bubua12.atlas.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dept")
public class SysDept {

    @TableId
    private Long deptId;
    private Long parentId;
    private String deptName;
    private Integer sort;
    private String leader;
    private String phone;
    private Integer status;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
