package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.system.repository.SysConfig;
import com.bubua12.atlas.system.mapper.SysConfigMapper;
import com.bubua12.atlas.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统配置服务实现
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper mapper;

    /**
     * 根据配置键获取配置值。
     *
     * @param configKey 配置键
     * @return 配置值
     */
    @Override
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig config = mapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 根据配置类型查询配置列表。
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    @Override
    public List<SysConfig> listByType(String configType) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigType, configType);
        return mapper.selectList(wrapper);
    }

    /**
     * 更新配置值。
     *
     * @param configKey 配置键
     * @param configValue 配置值
     */
    @Override
    public void updateConfig(String configKey, String configValue) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig config = mapper.selectOne(wrapper);
        if (config != null) {
            config.setConfigValue(configValue);
            mapper.updateById(config);
        }
    }
}
