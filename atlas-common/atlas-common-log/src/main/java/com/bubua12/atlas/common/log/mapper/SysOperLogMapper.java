package com.bubua12.atlas.common.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bubua12.atlas.common.log.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
