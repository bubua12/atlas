package com.bubua12.atlas.auth.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private Long expiresIn;
}
