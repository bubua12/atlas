package com.bubua12.atlas.common.mybatis.enums;

import lombok.Getter;

/**
 * 数据权限类型枚举
 */
@Getter
public enum DataScopeType {

    /**
     * 全部数据权限
     */
    ALL(1, "全部数据"),

    /**
     * 本部门及下级部门数据权限
     */
    DEPT_AND_CHILD(2, "本部门及下级"),

    /**
     * 仅本部门数据权限
     */
    DEPT_ONLY(3, "仅本部门"),

    /**
     * 仅本人数据权限
     */
    SELF_ONLY(4, "仅本人"),

    /**
     * 自定义部门数据权限
     */
    CUSTOM(5, "自定义部门");

    private final Integer value;
    private final String label;

    DataScopeType(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public static DataScopeType valueOf(Integer value) {
        for (DataScopeType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return ALL;
    }
}
