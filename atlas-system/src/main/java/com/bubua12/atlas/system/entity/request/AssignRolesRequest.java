package com.bubua12.atlas.system.entity.request;

import lombok.Data;

import java.util.List;

/**
 * 给用户分配角色请求
 */
@Data
public class AssignRolesRequest {
    private Long userId;
    private List<Long> roleIds;
}
