package com.bubua12.atlas.monitor.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 在线用户管理控制器（查看在线用户、强制下线）
 */
@RestController
@RequestMapping("/online")
public class OnlineUserController {

    @GetMapping
    public CommonResult<List<Object>> list() {
        return CommonResult.success(new ArrayList<>());
    }

    @DeleteMapping("/{tokenId}")
    public CommonResult<Void> kickOut(@PathVariable String tokenId) {
        // TODO: Implement kicking user offline by invalidating token
        return CommonResult.success();
    }
}
