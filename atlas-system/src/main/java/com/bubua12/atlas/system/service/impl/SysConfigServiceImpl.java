package com.bubua12.atlas.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bubua12.atlas.system.entity.SysConfig;
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

    @Override
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig config = mapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public List<SysConfig> listByType(String configType) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigType, configType);
        return mapper.selectList(wrapper);
    }

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
