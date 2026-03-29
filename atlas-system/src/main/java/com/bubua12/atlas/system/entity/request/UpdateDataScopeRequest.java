package com.bubua12.atlas.system.entity.request;

import lombok.Data;

import java.util.List;

/**
 * 更新角色数据权限请求体
 */
@Data
public class UpdateDataScopeRequest {
    private Long roleId;
    private Integer dataScope;
    private List<Long> deptIds;
}
