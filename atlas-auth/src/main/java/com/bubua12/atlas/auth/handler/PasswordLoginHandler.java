package com.bubua12.atlas.auth.handler;

import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.auth.feign.AtlasSystemFeign;
import com.bubua12.atlas.auth.form.LoginRequest;
import com.bubua12.atlas.common.core.result.CommonResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 用户名密码登录处理器
 * 通过 Feign 调用 atlas-system 服务校验用户名和密码。
 */
@Component
public class PasswordLoginHandler implements LoginHandler {

    @Resource
    private AtlasSystemFeign atlasSystemFeign;

    @Override
    public String grantType() {
        return "password";
    }

    /**
     * 密码登录认证流程：
     * 1. 验证用户名和密码是否匹配
     * 2. 查询用户信息
     */
    @Override
    public UserDTO authenticate(LoginRequest request) {
        // 验证密码
        CommonResult<Boolean> verifyResult = atlasSystemFeign.verifyPassword(
                request.getUsername(),
                request.getPassword()
        );

        // 从用户名错误以及密码错误调整为统一提示，防止用户名枚举破解
        if (verifyResult == null || verifyResult.getData() == null || !verifyResult.getData()) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 密码验证通过，查询用户信息
        CommonResult<UserDTO> result = atlasSystemFeign.getUserByUsername(request.getUsername());
        if (result == null || result.getData() == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        return result.getData();
    }
}
