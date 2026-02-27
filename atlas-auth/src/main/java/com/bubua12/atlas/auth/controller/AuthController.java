package com.bubua12.atlas.auth.controller;

import com.bubua12.atlas.auth.form.LoginBody;
import com.bubua12.atlas.auth.service.AuthService;
import com.bubua12.atlas.auth.vo.LoginVO;
import com.bubua12.atlas.common.core.result.CommonResult;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    public CommonResult<LoginVO> login(@Valid @RequestBody LoginBody loginBody) {
        LoginVO loginVO = authService.login(loginBody);
        return CommonResult.ok(loginVO);
    }

    @PostMapping("/logout")
    public CommonResult<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return CommonResult.ok();
    }

    @PostMapping("/refresh")
    public CommonResult<LoginVO> refresh(@RequestHeader("Authorization") String token) {
        LoginVO loginVO = authService.refreshToken(token);
        return CommonResult.ok(loginVO);
    }
}
