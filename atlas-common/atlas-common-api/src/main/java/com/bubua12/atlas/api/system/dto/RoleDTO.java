package com.bubua12.atlas.api.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色数据传输对象
 */
@Data
public class RoleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 角色权限标识
     */
    private String roleKey;
    /**
     * 状态（0正常 1停用）
     */
    private Integer status;
}
