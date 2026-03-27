package com.bubua12.atlas.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssignUsersRequest {
    private Long roleId;
    private List<Long> userIds;
}
