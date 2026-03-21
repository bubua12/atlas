package com.bubua12.atlas.auth.converter;

import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.common.core.converter.BaseConverter;
import com.bubua12.atlas.common.core.model.LoginUser;
import org.mapstruct.Mapper;

/**
 *
 *
 * @author bubua12
 * @since 2026/03/22 0:10
 */
@Mapper(componentModel = "spring")
public abstract class AuthConverter extends BaseConverter<LoginUser, UserDTO> {

}
