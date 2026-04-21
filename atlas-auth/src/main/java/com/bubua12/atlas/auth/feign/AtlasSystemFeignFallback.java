package com.bubua12.atlas.auth.feign;

import com.bubua12.atlas.api.system.dto.SysUserDTO;
import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.common.core.result.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AtlasSystemFeignFallback implements AtlasSystemFeign {

    @Override
    public CommonResult<UserDTO> getUserByUsername(String username) {
        log.error("atlas-system 熔断，getUserByUsername: {}", username);
        return CommonResult.fail("系统服务暂不可用");
    }

    @Override
    public CommonResult<UserDTO> getUserById(Long userId) {
        log.error("atlas-system 熔断，getUserById: {}", userId);
        return CommonResult.fail("系统服务暂不可用");
    }

    @Override
    public CommonResult<UserDTO> getUserByPhone(String phone) {
        log.error("atlas-system 熔断，getUserByPhone: {}", phone);
        return CommonResult.fail("系统服务暂不可用");
    }

    @Override
    public CommonResult<UserDTO> getUserByOpenId(String openId) {
        log.error("atlas-system 熔断，getUserByOpenId: {}", openId);
        return CommonResult.fail("系统服务暂不可用");
    }

    @Override
    public CommonResult<Boolean> verifyPassword(String username, String password) {
        log.error("atlas-system 熔断，verifyPassword: {}", username);
        return CommonResult.fail("系统服务暂不可用");
    }

    @Override
    public CommonResult<Void> createUserOAuth2(SysUserDTO sysUserDTO) {
        log.error("atlas-system 熔断，createUserOAuth2: {}", sysUserDTO);
        return CommonResult.fail("系统服务暂不可用");
    }
}
