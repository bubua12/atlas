package com.bubua12.atlas.system.converter;

import com.bubua12.atlas.api.system.dto.SysUserDTO;
import com.bubua12.atlas.common.core.converter.BaseConverter;
import com.bubua12.atlas.system.repository.SysUser;
import org.mapstruct.Mapper;

/**
 *
 *
 * @author bubua12
 * @since 2026/03/15 23:59
 */
@Mapper(componentModel = "spring")
public abstract class SysUserConverter extends BaseConverter<SysUserDTO, SysUser> {

}
