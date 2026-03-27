package com.bubua12.atlas.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateDataScopeRequest {
    private Long roleId;
    private Integer dataScope;
    private List<Long> deptIds;
}
