package com.bubua12.atlas.common.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 网关签发给下游服务的用户上下文。
 *
 * <p>这里刻意没有直接复用 {@link LoginUser}：
 * 一方面避免把 token、登录时间等下游不需要信任的字段一起透传；
 * 另一方面可以把“网关签名载荷”稳定成一个更小的协议对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayUserContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private Set<String> permissions;

    private Long deptId;

    private Integer dataScope;

    /**
     * 从 Redis 中的登录态投影出“需要下游信任”的最小身份集。
     */
    public static GatewayUserContext fromLoginUser(LoginUser loginUser) {
        GatewayUserContext userContext = new GatewayUserContext();
        userContext.setUserId(loginUser.getUserId());
        userContext.setUsername(loginUser.getUsername());
        userContext.setPermissions(loginUser.getPermissions() == null
                ? null
                : new HashSet<>(loginUser.getPermissions()));
        userContext.setDeptId(loginUser.getDeptId());
        userContext.setDataScope(loginUser.getDataScope());
        return userContext;
    }

    /**
     * 下游验签通过后，再恢复成统一的 {@link LoginUser} 上下文，复用现有权限和数据权限逻辑。
     */
    public LoginUser toLoginUser() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUsername(username);
        loginUser.setPermissions(permissions == null ? null : new HashSet<>(permissions));
        loginUser.setDeptId(deptId);
        loginUser.setDataScope(dataScope);
        return loginUser;
    }
}
