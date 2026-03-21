package com.bubua12.atlas.auth.feign;

import com.bubua12.atlas.api.system.dto.SysUserDTO;
import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * atlas-system 用户服务 Feign 客户端
 * 提供用户信息的远程查询能力，供各登录处理器调用。
 *
 * @author bubua12
 * @since 2026/02/27 22:20
 */
@FeignClient(value = "atlas-system", path = "/user")
public interface AtlasSystemFeign {

    /**
     * 根据用户名查询用户信息（密码登录使用）
     */
    @GetMapping("/info/{username}")
    CommonResult<UserDTO> getUserByUsername(@PathVariable String username);

    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/{userId}")
    CommonResult<UserDTO> getUserById(@PathVariable Long userId);

    /**
     * 根据手机号查询用户信息（验证码登录使用）
     */
    @GetMapping("/phone/{phone}")
    CommonResult<UserDTO> getUserByPhone(@PathVariable String phone);

    /**
     * 根据微信 openId 查询用户信息（微信登录使用）
     */
    @GetMapping("/openid/{openId}")
    CommonResult<UserDTO> getUserByOpenId(@PathVariable String openId);

    /**
     * 验证用户密码
     */
    @PostMapping("/verify-password")
    CommonResult<Boolean> verifyPassword(@RequestParam("username") String username,
                                         @RequestParam("password") String password);

    /**
     * OAuth2创建用户
     */
    @PostMapping("/oauth2/createUser")
    CommonResult<Void> createUserOAuth2(@RequestBody SysUserDTO sysUserDTO);
}
