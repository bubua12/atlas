package com.bubua12.atlas.common.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
