package com.bubua12.atlas.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dict_data")
public class SysDictData {

    @TableId
    private Long dictCode;
    private Integer dictSort;
    private String dictLabel;
    private String dictValue;
    private String dictType;
    private Integer status;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
