package com.bubua12.atlas.system.entity.request;

import lombok.Data;

import java.util.List;

/**
 * 给角色分配用户请求体
 */
@Data
public class AssignUsersRequest {
    private Long roleId;
    private List<Long> userIds;
}
