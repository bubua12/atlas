package com.bubua12.atlas.auth.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 旧版登录请求体（仅支持用户名密码登录）
 * 已被 {@link LoginRequest} 替代，保留兼容。
 */
@Data
public class LoginBody {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
