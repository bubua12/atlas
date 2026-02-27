package com.bubua12.atlas.common.core.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
