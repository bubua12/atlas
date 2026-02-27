package com.bubua12.atlas.auth.feign;

import com.bubua12.atlas.api.system.dto.UserDTO;
import com.bubua12.atlas.common.core.result.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 *
 * @author bubua12
 * @since 2026/02/27 22:20
 */
@FeignClient(value = "atlas-system", path = "/user")
public interface AtlasSystemFeign {
    @GetMapping("/info/{username}")
    CommonResult<UserDTO> getUserByUsername(@PathVariable(value = "username") String username);

    @GetMapping("/{userId}")
    CommonResult<UserDTO> getUserById(@PathVariable("userId") Long userId);
}
