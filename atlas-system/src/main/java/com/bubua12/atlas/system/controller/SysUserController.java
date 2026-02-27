package com.bubua12.atlas.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.common.core.domain.PageQuery;
import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.system.entity.SysUser;
import com.bubua12.atlas.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping
    public CommonResult<IPage<SysUser>> list(PageQuery query) {
        return CommonResult.ok(sysUserService.list(query));
    }

    @GetMapping("/{userId}")
    public CommonResult<SysUser> getById(@PathVariable Long userId) {
        return CommonResult.ok(sysUserService.getById(userId));
    }

    @GetMapping("/info/{username}")
    public CommonResult<UserDTO> getUserByUsername(@PathVariable("username") String username) {
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return CommonResult.fail("User not found");
        }
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setPassword(user.getPassword());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setDeptId(user.getDeptId());
        return CommonResult.ok(dto);
    }

    @PostMapping
    public CommonResult<Void> create(@RequestBody SysUser user) {
        sysUserService.create(user);
        return CommonResult.ok();
    }

    @PutMapping
    public CommonResult<Void> update(@RequestBody SysUser user) {
        sysUserService.update(user);
        return CommonResult.ok();
    }

    @DeleteMapping("/{userId}")
    public CommonResult<Void> delete(@PathVariable Long userId) {
        sysUserService.delete(userId);
        return CommonResult.ok();
    }

}
