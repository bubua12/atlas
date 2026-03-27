package com.bubua12.atlas.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssignMenusRequest {
    private Long roleId;
    private List<Long> menuIds;
}
