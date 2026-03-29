package com.bubua12.atlas.system.entity.request;

import lombok.Data;

import java.util.List;

/**
 * 给角色分配菜单请求体
 */
@Data
public class AssignMenusRequest {
    private Long roleId;
    private List<Long> menuIds;
}
