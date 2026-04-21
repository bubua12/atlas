package com.bubua12.atlas.monitor.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.monitor.service.OnlineUserService;
import com.bubua12.atlas.monitor.entity.vo.OnlineUserVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线用户管理控制器（查看在线用户、强制下线）
 */
@RestController
@RequestMapping("/online")
public class OnlineUserController {

    @Resource
    private OnlineUserService onlineUserService;

    @GetMapping
    public CommonResult<List<OnlineUserVO>> list() {
        return CommonResult.success(onlineUserService.listOnlineUsers());
    }

    @DeleteMapping("/{token}")
    public CommonResult<Void> kickOut(@PathVariable String token) {
        onlineUserService.kickOut(token);
        return CommonResult.success();
    }
}
