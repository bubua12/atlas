package com.bubua12.atlas.system.service;

import com.bubua12.atlas.system.entity.SysConfig;

import java.util.List;

/**
 * 系统配置服务
 */
public interface SysConfigService {

    /**
     * 根据配置键获取配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据类型查询配置列表
     */
    List<SysConfig> listByType(String configType);

    /**
     * 更新配置
     */
    void updateConfig(String configKey, String configValue);
}
