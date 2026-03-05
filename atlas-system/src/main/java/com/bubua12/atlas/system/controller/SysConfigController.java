package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.security.annotation.RequiresPermission;
import com.bubua12.atlas.system.entity.SysConfig;
import com.bubua12.atlas.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    @RequiresPermission("system:config:list")
    @GetMapping("/type/{configType}")
    public CommonResult<List<SysConfig>> listByType(@PathVariable String configType) {
        return CommonResult.success(configService.listByType(configType));
    }

    // fixme 不标准的请求体
    @RequiresPermission("system:config:edit")
    @PutMapping
    public CommonResult<Void> updateConfig(@RequestBody SysConfig config) {
        configService.updateConfig(config.getConfigKey(), config.getConfigValue());
        return CommonResult.success();
    }
}
