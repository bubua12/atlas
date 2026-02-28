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
     * 1. 根据用户名远程查询用户信息
     * 2. 校验密码是否匹配
     */
    @Override
    public UserDTO authenticate(LoginRequest request) {
        CommonResult<UserDTO> result = atlasSystemFeign.getUserByUsername(request.getUsername());
        if (result == null || result.getData() == null) {
            throw new RuntimeException("用户不存在: " + request.getUsername());
        }
        UserDTO user = result.getData();
        if (request.getPassword() == null || !request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }
}
