package com.bubua12.atlas.api.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String nickname;
    private String password;
    private String email;
    private String phone;
    private Integer status;
    private Long deptId;
    private List<Long> roleIds;
    private Set<String> permissions;
}
