package com.bubua12.atlas.common.mybatis.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.bubua12.atlas.common.core.model.LoginUser;
import com.bubua12.atlas.common.mybatis.annotation.DataScope;
import com.bubua12.atlas.common.mybatis.handler.DataScopeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 数据权限拦截器
 * @deprecated
 * @see DataScopeInnerInterceptor
 */
@Slf4j
@RequiredArgsConstructor
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class DataScopeInterceptor implements Interceptor {

    private final DataScopeHandler dataScopeHandler;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];

        // 获取 Mapper 方法上的 @DataScope 注解
        DataScope dataScope = getDataScopeAnnotation(ms);
        if (dataScope == null) {
            return invocation.proceed();
        }

        // 获取当前用户
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return invocation.proceed();
        }
        LoginUser loginUser = (LoginUser) attrs.getRequest().getAttribute("loginUser");
        if (loginUser == null) {
            return invocation.proceed();
        }

        // 生成数据权限过滤条件
        String condition = dataScopeHandler.buildDataScopeCondition(dataScope, loginUser);
        log.info("生成数据权限过滤条件：{}", condition);
        if (condition == null || condition.isEmpty()) {
            return invocation.proceed();
        }

        // 改写 SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        String newSql = rewriteSql(originalSql, condition);
        log.info("替换的SQL: {}", newSql);

        // 替换 SQL
        PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
        mpBoundSql.sql(newSql);

        return invocation.proceed();
    }

    /**
     * 获取 Mapper 方法上的 @DataScope 注解
     */
    private DataScope getDataScopeAnnotation(MappedStatement ms) {
        try {
            String mapperId = ms.getId();
            String className = mapperId.substring(0, mapperId.lastIndexOf("."));
            String methodName = mapperId.substring(mapperId.lastIndexOf(".") + 1);

            Class<?> mapperClass = Class.forName(className);
            for (Method method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName) && method.isAnnotationPresent(DataScope.class)) {
                    return method.getAnnotation(DataScope.class);
                }
            }
        } catch (Exception e) {
            log.warn("获取 @DataScope 注解失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 改写 SQL，追加数据权限过滤条件
     */
    private String rewriteSql(String originalSql, String condition) {
        try {
            Statement statement = CCJSqlParserUtil.parse(originalSql);
            if (statement instanceof Select) {
                Select select = (Select) statement;
                PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                Expression where = plainSelect.getWhere();
                Expression dataScopeCondition = CCJSqlParserUtil.parseCondExpression(condition);

                if (where != null) {
                    plainSelect.setWhere(new AndExpression(where, dataScopeCondition));
                } else {
                    plainSelect.setWhere(dataScopeCondition);
                }
                return select.toString();
            }
        } catch (JSQLParserException e) {
            log.error("SQL 解析失败，使用原始 SQL: {}", e.getMessage());
        }
        return originalSql;
    }
}
