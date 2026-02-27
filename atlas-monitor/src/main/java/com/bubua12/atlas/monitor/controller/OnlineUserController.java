package com.bubua12.atlas.monitor.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/online")
public class OnlineUserController {

    @GetMapping
    public CommonResult<List<Object>> list() {
        return CommonResult.ok(new ArrayList<>());
    }

    @DeleteMapping("/{tokenId}")
    public CommonResult<Void> kickOut(@PathVariable String tokenId) {
        // TODO: Implement kicking user offline by invalidating token
        return CommonResult.ok();
    }
}
