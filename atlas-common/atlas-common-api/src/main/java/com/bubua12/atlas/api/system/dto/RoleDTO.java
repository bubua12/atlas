package com.bubua12.atlas.api.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;
    private String roleName;
    private String roleKey;
    private Integer status;
}
