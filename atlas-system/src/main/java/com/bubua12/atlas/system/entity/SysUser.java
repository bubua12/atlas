package com.bubua12.atlas.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId
    private Long userId;
    private String username;
    private String nickname;
    private String password;
    private String email;
    private String phone;
    private Integer sex;
    private String avatar;
    private Integer status;
    private Long deptId;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
