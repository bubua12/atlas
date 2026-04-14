package com.bubua12.atlas.api.auth.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 权限变更事件。
 */
@Data
public class PermissionChangeEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 受影响的用户ID集合。
     */
    private Set<Long> userIds;

    /**
     * 触发原因，便于日志定位。
     */
    private String reason;

    /**
     * 事件时间戳。
     */
    private Long timestamp;
}
