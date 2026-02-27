package com.bubua12.atlas.common.security.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private String token;

    private Set<String> permissions;
}
