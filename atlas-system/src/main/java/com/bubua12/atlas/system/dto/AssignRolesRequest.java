package com.bubua12.atlas.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssignRolesRequest {
    private Long userId;
    private List<Long> roleIds;
}
