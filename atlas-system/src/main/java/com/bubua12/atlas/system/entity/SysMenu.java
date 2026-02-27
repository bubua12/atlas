package com.bubua12.atlas.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId
    private Long menuId;
    private String menuName;
    private Long parentId;
    private Integer sort;
    private String path;
    private String component;
    /** M=directory, C=menu, F=button */
    private String menuType;
    private String perms;
    private String icon;
    private Integer visible;
    private Integer status;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
