package com.bubua12.atlas.system.controller;

import com.bubua12.atlas.common.core.result.CommonResult;
import com.bubua12.atlas.common.security.annotation.RequiresPermission;
import com.bubua12.atlas.system.repository.SysConfig;
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

    /**
     * 按配置类型查询配置列表。
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    @RequiresPermission("system:config:list")
    @GetMapping("/type/{configType}")
    public CommonResult<List<SysConfig>> listByType(@PathVariable String configType) {
        return CommonResult.success(configService.listByType(configType));
    }

    /**
     * 更新指定配置项的值。
     *
     * @param config 配置信息
     * @return 执行结果
     */
    @RequiresPermission("system:config:edit")
    @PutMapping
    public CommonResult<Void> updateConfig(@RequestBody SysConfig config) {
        configService.updateConfig(config.getConfigKey(), config.getConfigValue());
        return CommonResult.success();
    }
}
