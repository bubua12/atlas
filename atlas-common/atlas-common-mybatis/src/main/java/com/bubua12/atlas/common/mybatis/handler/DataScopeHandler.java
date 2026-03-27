package com.bubua12.atlas.common.mybatis.handler;

import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.mybatis.annotation.DataScope;
import com.bubua12.atlas.common.mybatis.enums.DataScopeType;
import lombok.extern.slf4j.Slf4j;


/**
 * 数据权限处理器
 */
@Slf4j
public class DataScopeHandler {

    /**
     * 生成数据权限 SQL 过滤条件
     */
    public String buildDataScopeCondition(DataScope dataScope, LoginUser loginUser) {
        if (loginUser == null) {
            return null;
        }

        // 超级管理员不过滤
        if (loginUser.getUserId() != null && loginUser.getUserId() == 1L) {
            return null;
        }

        Integer dataScopeValue = loginUser.getDataScope();
        if (dataScopeValue == null) {
            dataScopeValue = DataScopeType.ALL.getValue();
        }

        DataScopeType dataScopeType = DataScopeType.valueOf(dataScopeValue);

        return switch (dataScopeType) {
            case ALL -> null;
            case DEPT_AND_CHILD -> buildDeptAndChildCondition(dataScope, loginUser);
            case DEPT_ONLY -> buildDeptOnlyCondition(dataScope, loginUser);
            case SELF_ONLY -> buildSelfOnlyCondition(dataScope, loginUser);
            case CUSTOM -> buildCustomDeptCondition(dataScope, loginUser);
        };
    }

    /**
     * 本部门及下级部门条件
     */
    private String buildDeptAndChildCondition(DataScope dataScope, LoginUser loginUser) {
        if (dataScope.deptAlias().isEmpty() || loginUser.getDeptId() == null) {
            return null;
        }
        // TODO: 实现部门树查询，获取当前部门及所有子部门ID
        // 暂时只过滤本部门
        return String.format("%s.%s = %d", dataScope.deptAlias(), dataScope.deptField(), loginUser.getDeptId());
    }

    /**
     * 仅本部门条件
     */
    private String buildDeptOnlyCondition(DataScope dataScope, LoginUser loginUser) {
        if (dataScope.deptAlias().isEmpty() || loginUser.getDeptId() == null) {
            return null;
        }
        return String.format("%s.%s = %d", dataScope.deptAlias(), dataScope.deptField(), loginUser.getDeptId());
    }

    /**
     * 仅本人条件
     */
    private String buildSelfOnlyCondition(DataScope dataScope, LoginUser loginUser) {
        if (dataScope.userAlias().isEmpty() || loginUser.getUserId() == null) {
            return null;
        }
        return String.format("%s.%s = %d", dataScope.userAlias(), dataScope.userField(), loginUser.getUserId());
    }

    /**
     * 自定义部门条件
     */
    private String buildCustomDeptCondition(DataScope dataScope, LoginUser loginUser) {
        if (dataScope.deptAlias().isEmpty()) {
            return null;
        }
        // TODO: 查询 sys_role_dept 表获取自定义部门ID列表
        // 暂时返回 null
        return null;
    }
}
