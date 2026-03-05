package com.bubua12.atlas.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bubua12.atlas.auth.entity.SysLoginFailRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录失败记录 Mapper
 */
@Mapper
public interface SysLoginFailRecordMapper extends BaseMapper<SysLoginFailRecord> {
}
