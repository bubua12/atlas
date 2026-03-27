> 一般的微服务系统，比如一些微服务管理平台，数据权限指的是什么意思呀？

● 数据权限是指控制用户能够访问和操作哪些数据范围的权限机制，与功能权限（能做什么操作）不同，它关注的是能看到/操作哪些数
据。

在微服务管理平台中，常见的数据权限场景：

1. 部门数据权限
- 全部数据：能看到所有部门的数据
- 本部门及下级部门：只能看到自己部门和子部门的数据
- 仅本部门：只能看到本部门数据
- 仅本人：只能看到自己创建/负责的数据

2. 典型应用场景
   // 用户 A（销售部经理）查询订单列表
   // → 只返回销售部及其下属团队的订单

// 用户 B（普通销售）查询订单列表
// → 只返回自己负责的客户订单

// 用户 C（超级管理员）查询订单列表
// → 返回全公司所有订单

3. 实现方式

通常在 SQL 查询时动态拼接过滤条件：

// 伪代码示例
SELECT * FROM sys_user
WHERE deleted = 0
AND dept_id IN (当前用户可见的部门ID列表)  -- 数据权限过滤

4. 与功能权限的区别

┌──────────┬────────────────┬──────────────────────────────────────┐
│   类型   │    控制内容    │                 示例                 │
├──────────┼────────────────┼──────────────────────────────────────┤
│ 功能权限 │ 能否执行某操作 │ system:user:list（能否查看用户列表） │
├──────────┼────────────────┼──────────────────────────────────────┤
│ 数据权限 │ 能看到哪些数据 │ 只能看到本部门的用户，而非全部用户   │
└──────────┴────────────────┴──────────────────────────────────────┘

5. 在 Atlas 项目中的潜在实现位置

如果要添加数据权限，通常会：
- 在 sys_role 表增加 data_scope 字段（1=全部，2=本部门及下级，3=仅本部门，4=仅本人）
- 在 MyBatis 拦截器或 AOP 中自动注入数据过滤条件
- 结合 sys_user 的 dept_id 和部门层级关系进行过滤

这样可以确保不同角色的用户在调用同一个接口时，自动看到符合其权限范围的数据。

────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
> 
────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
? for shortcuts
▐▛███▜▌   Claude Code v2.1.84
▝▜█████▛▘  Opus 4.6 (1M context) with medium effort · API Usage Billing
▘▘ ▝▝    D:\workspaces\IdeaProjects\atlas

> 一般的微服务系统，比如一些微服务管理平台，数据权限指的是什么意思呀？

● 数据权限是指控制用户能够访问和操作哪些数据范围的权限机制，与功能权限（能做什么操作）不同，它关注的是能看到/操作哪些数
据。

在微服务管理平台中，常见的数据权限场景：

1. 部门数据权限
- 全部数据：能看到所有部门的数据
- 本部门及下级部门：只能看到自己部门和子部门的数据
- 仅本部门：只能看到本部门数据
- 仅本人：只能看到自己创建/负责的数据

2. 典型应用场景
   // 用户 A（销售部经理）查询订单列表
   // → 只返回销售部及其下属团队的订单

// 用户 B（普通销售）查询订单列表
// → 只返回自己负责的客户订单

// 用户 C（超级管理员）查询订单列表
// → 返回全公司所有订单

3. 实现方式

通常在 SQL 查询时动态拼接过滤条件：

// 伪代码示例
SELECT * FROM sys_user
WHERE deleted = 0
AND dept_id IN (当前用户可见的部门ID列表)  -- 数据权限过滤

4. 与功能权限的区别

┌──────────┬────────────────┬──────────────────────────────────────┐
│   类型   │    控制内容    │                 示例                 │
├──────────┼────────────────┼──────────────────────────────────────┤
│ 功能权限 │ 能否执行某操作 │ system:user:list（能否查看用户列表） │
├──────────┼────────────────┼──────────────────────────────────────┤
│ 数据权限 │ 能看到哪些数据 │ 只能看到本部门的用户，而非全部用户   │
└──────────┴────────────────┴──────────────────────────────────────┘

5. 在 Atlas 项目中的潜在实现位置

如果要添加数据权限，通常会：
- 在 sys_role 表增加 data_scope 字段（1=全部，2=本部门及下级，3=仅本部门，4=仅本人）
- 在 MyBatis 拦截器或 AOP 中自动注入数据过滤条件
- 结合 sys_user 的 dept_id 和部门层级关系进行过滤

这样可以确保不同角色的用户在调用同一个接口时，自动看到符合其权限范围的数据。

> 我想要实现一个注解驱动的数据权限功能，你先给我一个实现的方案以及思路

● 我来给你设计一个注解驱动的数据权限方案，适配 Atlas 项目的架构风格。

实现方案

核心思路

1. 注解定义：在 Mapper 方法上标注 @DataScope，声明需要过滤的表别名和字段
2. MyBatis 拦截器：拦截 SQL 执行，动态注入 WHERE 条件
3. 权限计算：从当前用户上下文获取角色的数据权限范围，计算可见的部门/用户 ID 列表
4. SQL 改写：使用 JSqlParser 解析并修改原始 SQL

  ---
技术选型

┌─────────────────────┬────────────────────────────────────────────┐
│        组件         │                    作用                    │
├─────────────────────┼────────────────────────────────────────────┤
│ @DataScope 注解     │ 标记需要数据权限过滤的方法                 │
├─────────────────────┼────────────────────────────────────────────┤
│ MyBatis Interceptor │ 拦截 Executor.query() 和 Executor.update() │
├─────────────────────┼────────────────────────────────────────────┤
│ JSqlParser          │ 解析和改写 SQL（已在项目中使用）           │
├─────────────────────┼────────────────────────────────────────────┤
│ ThreadLocal         │ 存储当前用户的数据权限上下文               │
└─────────────────────┴────────────────────────────────────────────┘

  ---
数据库设计

扩展 sys_role 表：
ALTER TABLE sys_role ADD COLUMN data_scope TINYINT DEFAULT 1 COMMENT '数据权限范围：1=全部 2=本部门及下级 3=仅本部门
4=仅本人 5=自定义部门';

新增自定义部门权限表（可选）：
CREATE TABLE sys_role_dept (
role_id BIGINT NOT NULL COMMENT '角色ID',
dept_id BIGINT NOT NULL COMMENT '部门ID',
PRIMARY KEY (role_id, dept_id)
) COMMENT='角色-部门关联表（用于自定义数据权限）';

  ---
实现步骤

1. 定义注解

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
/** 部门表别名（用于过滤部门） */
String deptAlias() default "";

      /** 用户表别名（用于过滤用户） */
      String userAlias() default "";
}

2. 数据权限上下文

public class DataScopeContext {
private Integer dataScope;      // 数据权限类型
private Long userId;            // 当前用户ID
private Long deptId;            // 当前用户部门ID
private Set<Long> deptIds;      // 可见部门ID集合
}

3. MyBatis 拦截器

拦截 SQL 执行，检测到 @DataScope 注解时：
- 从 SecurityContextHolder 获取当前用户
- 根据用户角色的 data_scope 计算可见范围
- 使用 JSqlParser 在 WHERE 子句中追加过滤条件

4. SQL 改写逻辑

原始 SQL：
SELECT u.* FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id = d.id WHERE u.deleted = 0

改写后（假设用户只能看本部门）：
SELECT u.* FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id = d.id
WHERE u.deleted = 0 AND u.dept_id = 10

  ---
使用示例

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

      @DataScope(deptAlias = "d", userAlias = "u")
      List<SysUser> selectUserList(@Param("query") UserQuery query);
}

拦截器会自动根据当前用户的数据权限，在 SQL 中注入：
- d.id IN (可见部门ID列表) 或
- u.id = 当前用户ID

  ---
优势

- 无侵入：业务代码只需加注解，无需手动拼接 SQL
- 灵活：支持多表关联场景（通过别名区分）
- 统一：与现有的 @RequiresPermission 风格一致
- 高效：拦截器层面处理，避免重复逻辑

  ---
注意事项

1. 超级管理员豁免：User ID = 1 跳过数据权限过滤
2. 部门层级查询：需要递归查询部门树（可用 MySQL 8 的 CTE 或缓存部门关系）
3. 性能优化：可见部门列表建议缓存到 Redis
4. 多角色处理：用户有多个角色时，取最宽松的数据权限

  ---
需要我开始实现具体代码吗？我会按照以下顺序：
1. 注解定义
2. 数据权限上下文类
3. MyBatis 拦截器
4. 部门层级查询工具
5. 使用示例

> 那@DataScope你打算怎么设计？

● 我建议 @DataScope 注解设计如下：

/**
* 数据权限过滤注解
* 用于 Mapper 方法，自动根据用户角色的数据权限范围过滤查询结果
  */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface DataScope {

  /**
    * 部门表的别名
    * 用于在 SQL 中定位部门字段进行过滤
    * 例如：SELECT * FROM sys_user u LEFT JOIN sys_dept d ...
    * 则 deptAlias = "d"
      */
      String deptAlias() default "";

  /**
    * 用户表的别名
    * 用于在 SQL 中定位用户字段进行过滤（仅本人数据权限时使用）
    * 例如：SELECT * FROM sys_user u ...
    * 则 userAlias = "u"
      */
      String userAlias() default "";

  /**
    * 部门字段名
    * 默认为 dept_id，如果表中部门字段名不同可自定义
      */
      String deptField() default "dept_id";

  /**
    * 用户字段名
    * 默认为 user_id 或 id，用于"仅本人"数据权限过滤
      */
      String userField() default "id";
      }

设计考量

1. 最小化配置

- 只需指定表别名，字段名有合理默认值
- 大部分场景只需 @DataScope(deptAlias = "d")

2. 灵活性

- 支持单表查询：@DataScope(userAlias = "u")
- 支持关联查询：@DataScope(deptAlias = "d", userAlias = "u")
- 支持自定义字段名（适配不同表结构）

3. 使用示例

// 场景1：用户列表（关联部门表）
@DataScope(deptAlias = "d", userAlias = "u")
List<SysUser> selectUserList(@Param("query") UserQuery query);

// 场景2：订单列表（单表，通过 create_user_id 过滤）
@DataScope(userAlias = "o", userField = "create_user_id")
List<Order> selectOrderList(@Param("query") OrderQuery query);

// 场景3：部门列表（直接过滤部门表）
@DataScope(deptAlias = "d", deptField = "id")
List<SysDept> selectDeptList(@Param("query") DeptQuery query);

4. 拦截器处理逻辑

根据注解参数和用户数据权限类型，生成不同的 SQL 片段：

┌──────────────┬───────────────────────────┐
│ 数据权限类型 │      生成的 SQL 条件      │
├──────────────┼───────────────────────────┤
│ 全部数据     │ 不追加条件                │
├──────────────┼───────────────────────────┤
│ 本部门及下级 │ d.dept_id IN (1,2,3,...)  │
├──────────────┼───────────────────────────┤
│ 仅本部门     │ d.dept_id = 10            │
├──────────────┼───────────────────────────┤
│ 仅本人       │ u.id = 100                │
├──────────────┼───────────────────────────┤
│ 自定义部门   │ d.dept_id IN (5,8,12,...) │
└──────────────┴───────────────────────────┘

这样设计够用吗？还是需要增加其他配置项？

> 这个注解使用一般标注在什么上面呀

● @DataScope 注解标注在 Mapper 接口的方法上。

标注位置

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

      // ✅ 正确：标注在 Mapper 方法上
      @DataScope(deptAlias = "d", userAlias = "u")
      List<SysUser> selectUserList(@Param("query") UserQuery query);

      @DataScope(deptAlias = "d")
      IPage<SysUser> selectUserPage(IPage<SysUser> page, @Param("query") UserQuery query);
}

为什么是 Mapper 层？

1. 拦截时机合适
- MyBatis 拦截器在 SQL 执行前介入，可以直接改写 SQL
- 比在 Service 层手动拼接条件更优雅

2. 与 SQL 强关联
- 注解参数（表别名、字段名）直接对应 SQL 结构
- 拦截器可以通过反射获取方法上的注解

3. 无侵入业务逻辑
- Service 层无需感知数据权限实现细节
- Controller 和 Service 代码保持简洁

完整调用链

Controller↓
Service (无需关心数据权限)
↓
Mapper 方法 (@DataScope 注解)
↓
MyBatis 拦截器 (检测注解 → 改写 SQL)
↓
数据库执行 (已注入权限过滤条件)

对比其他方案

┌────────────────────┬──────────────────────────────┬──────────────────────────────────┐
│        方案        │           标注位置           │               缺点               │
├────────────────────┼──────────────────────────────┼──────────────────────────────────┤
│ ❌ Controller 方法 │ @DataScope 在 @GetMapping 上 │ 无法直接影响 SQL，需要传参到底层 │
├────────────────────┼──────────────────────────────┼──────────────────────────────────┤
│ ❌ Service 方法    │ @DataScope 在 Service 上     │ 同样无法直接改写 SQL             │
├────────────────────┼──────────────────────────────┼──────────────────────────────────┤
│ ✅ Mapper 方法     │ @DataScope 在 Mapper 上      │ 拦截器可直接获取并处理           │
└────────────────────┴──────────────────────────────┴──────────────────────────────────┘

所以标准用法就是：在需要数据权限过滤的 Mapper 查询方法上加 @DataScope 注解。

> 为啥SysRole还要加字段呢，为啥来着？

● 因为数据权限是绑定在角色上的，不同角色看到的数据范围不同。

为什么要在 sys_role 加 data_scope 字段？

核心逻辑：
- 用户 → 拥有角色 → 角色决定数据权限范围

举例说明：

角色A：销售经理
- 功能权限：system:user:list（能查用户列表）
- 数据权限：本部门及下级（data_scope = 2）
→ 能看到销售部及下属团队的所有用户

角色B：普通销售
- 功能权限：system:user:list（能查用户列表）
- 数据权限：仅本人（data_scope = 4）
→ 只能看到自己的数据

角色C：超级管理员
- 功能权限：*:*:*（所有权限）
- 数据权限：全部数据（data_scope = 1）
→ 能看到所有用户

数据权限类型枚举

1 = 全部数据         // 不做任何过滤
2 = 本部门及下级部门  // dept_id IN (当前部门 + 子部门)
3 = 仅本部门         // dept_id = 当前部门
4 = 仅本人           // user_id = 当前用户
5 = 自定义部门       // dept_id IN (sys_role_dept 表配置的部门)

工作流程

1. 用户登录 → 查询用户的所有角色
2. 获取角色的 data_scope 值（取最宽松的）
3. 执行 Mapper 查询时，拦截器读取 data_scope
4. 根据 data_scope 计算可见的部门/用户 ID
5. 动态注入 SQL 过滤条件

为什么不放在用户表？

如果放在 sys_user 表：
- ❌ 一个用户可能有多个角色，每个角色的数据权限不同
- ❌ 修改数据权限需要逐个修改用户，而不是批量修改角色
- ❌ 违反 RBAC 设计原则（权限应该绑定在角色上）

所以数据权限和功能权限一样，都是角色的属性。

> 那这个注解你推荐是放在什么地方呀

● 推荐放在 atlas-common-mybatis 模块中。

推荐位置

atlas-common/
└── atlas-common-mybatis/
└── src/main/java/com/atlas/common/mybatis/├── annotation/
│   └── DataScope.java          ← 注解定义
├── interceptor/
│   └── DataScopeInterceptor.java  ← MyBatis 拦截器
├── handler/
│   └── DataScopeHandler.java      ← 数据权限处理逻辑
└── enums/
└── DataScopeType.java         ← 数据权限类型枚举

为什么是 atlas-common-mybatis？

1. 职责匹配
- 数据权限本质是 SQL 层面的过滤
- 与 MyBatis 拦截器强绑定
- 和分页插件、SQL 日志等属于同一层次

2. 复用性
- 所有业务服务（atlas-system、atlas-auth、atlas-infra）都依赖 atlas-common-mybatis
- 一次实现，全局可用

3. 依赖关系清晰
   atlas-common-mybatis├── 依赖 atlas-common-core（基础工具类）
   └── 被依赖 atlas-system-biz、atlas-auth 等业务模块

4. 与现有架构一致
- 项目已有 atlas-common-security（功能权限）
- atlas-common-mybatis（数据权限）形成对称设计

包结构建议

com.atlas.common.mybatis.annotation.DataScope       // 注解
com.atlas.common.mybatis.enums.DataScopeType        // 枚举：1=全部 2=本部门及下级...
com.atlas.common.mybatis.interceptor.DataScopeInterceptor  // 拦截器
com.atlas.common.mybatis.handler.DataScopeHandler   // 权限计算 + SQL 改写

需要我开始实现吗？

> 你说的这个原理是怎么实现来着？MyBatis这块我不是特别的懂，帮我讲解一下先

● 我来详细讲解 MyBatis 拦截器实现数据权限的原理。

核心原理

MyBatis 提供了插件机制，允许在 SQL 执行的关键节点进行拦截和修改。

  ---
1. MyBatis 执行流程

用户调用 Mapper 方法
↓
MyBatis 解析方法和注解
↓
生成 MappedStatement（包含 SQL、参数映射等）
↓
【拦截点】Executor.query() / update()  ← 我们在这里拦截
↓
执行 SQL
↓
返回结果

  ---
2. 拦截器工作原理

步骤 1：定义拦截器

@Intercepts({
@Signature(
type = Executor.class,           // 拦截 Executor
method = "query",                // 拦截 query 方法
args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
),
@Signature(
type = Executor.class,
method = "update",               // 拦截 update 方法（insert/update/delete）
args = {MappedStatement.class, Object.class}
)
})
@Component
public class DataScopeInterceptor implements Interceptor {

      @Override
      public Object intercept(Invocation invocation) throws Throwable {
          // 这里可以修改 SQL
      }
}

关键点：
- @Intercepts 声明要拦截的方法
- Executor.query() 是所有查询的入口
- Executor.update() 是所有增删改的入口

  ---
步骤 2：获取原始 SQL

@Override
public Object intercept(Invocation invocation) throws Throwable {
// 1. 获取 MappedStatement（包含 SQL 信息）
MappedStatement ms = (MappedStatement) invocation.getArgs()[0];

      // 2. 获取 BoundSql（包含完整的 SQL 和参数）
      Object parameter = invocation.getArgs()[1];
      BoundSql boundSql = ms.getBoundSql(parameter);

      // 3. 获取原始 SQL
      String originalSql = boundSql.getSql();
      System.out.println("原始SQL: " + originalSql);
      // 例如：SELECT * FROM sys_user WHERE deleted = 0

      // 继续执行...
}

  ---
步骤 3：检查是否需要数据权限过滤

// 4. 获取 Mapper 方法
String mapperId = ms.getId(); // 例如：com.atlas.system.mapper.SysUserMapper.selectUserList
Class<?> mapperClass = Class.forName(mapperId.substring(0, mapperId.lastIndexOf(".")));
String methodName = mapperId.substring(mapperId.lastIndexOf(".") + 1);

Method method = null;
for (Method m : mapperClass.getMethods()) {
if (m.getName().equals(methodName)) {
method = m;
break;
}
}

// 5. 检查方法上是否有 @DataScope 注解
if (method == null || !method.isAnnotationPresent(DataScope.class)) {
// 没有注解，直接放行
return invocation.proceed();
}

DataScope dataScope = method.getAnnotation(DataScope.class);
String deptAlias = dataScope.deptAlias();
String userAlias = dataScope.userAlias();

  ---
步骤 4：获取当前用户的数据权限

// 6. 从 ThreadLocal 获取当前用户信息
LoginUser loginUser = SecurityContextHolder.getLoginUser();
if (loginUser == null) {
return invocation.proceed(); // 未登录，放行
}

// 7. 获取用户的数据权限类型（从角色中取最宽松的）
Integer dataScopeType = loginUser.getDataScope(); // 1=全部 2=本部门及下级 3=仅本部门 4=仅本人

if (dataScopeType == 1) {
return invocation.proceed(); // 全部数据权限，不过滤
}

  ---
步骤 5：计算可见的数据范围

// 8. 根据数据权限类型计算过滤条件
String filterCondition = "";

switch (dataScopeType) {
case 2: // 本部门及下级部门
Set<Long> deptIds = getDeptIdsWithChildren(loginUser.getDeptId());
filterCondition = deptAlias + ".dept_id IN (" + String.join(",", deptIds) + ")";
break;

      case 3: // 仅本部门
          filterCondition = deptAlias + ".dept_id = " + loginUser.getDeptId();
          break;

      case 4: // 仅本人
          filterCondition = userAlias + ".id = " + loginUser.getUserId();
          break;
}

  ---
步骤 6：改写 SQL

// 9. 使用 JSqlParser 解析并修改 SQL
Statement statement = CCJSqlParserUtil.parse(originalSql);
Select select = (Select) statement;
PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

// 10. 获取原有的 WHERE 条件
Expression where = plainSelect.getWhere();

// 11. 追加数据权限过滤条件
Expression dataScopeCondition = CCJSqlParserUtil.parseCondExpression(filterCondition);

if (where != null) {
// 原有条件 AND 数据权限条件
AndExpression newWhere = new AndExpression(where, dataScopeCondition);
plainSelect.setWhere(newWhere);
} else {
// 没有 WHERE 子句，直接设置
plainSelect.setWhere(dataScopeCondition);
}

String newSql = select.toString();
System.out.println("改写后SQL: " + newSql);
// 例如：SELECT * FROM sys_user WHERE deleted = 0 AND dept_id = 10

  ---
步骤 7：替换 SQL 并执行

// 12. 反射修改 BoundSql 中的 SQL
Field sqlField = BoundSql.class.getDeclaredField("sql");
sqlField.setAccessible(true);
sqlField.set(boundSql, newSql);

// 13. 继续执行（使用修改后的 SQL）
return invocation.proceed();

  ---
3. 完整流程示意图

Mapper.selectUserList()
↓
拦截器拦截
↓
检查 @DataScope 注解 → 有
↓
获取当前用户 → User ID=100, Dept ID=10, DataScope=3（仅本部门）
↓
原始SQL: SELECT * FROM sys_user u WHERE u.deleted = 0
↓
计算过滤条件: u.dept_id = 10
↓
改写SQL: SELECT * FROM sys_user u WHERE u.deleted = 0 AND u.dept_id = 10
↓
执行改写后的SQL
↓
返回结果（只包含本部门用户）

  ---
4. 关键技术点

┌─────────────────────┬──────────────────────────┐
│        技术         │           作用           │
├─────────────────────┼──────────────────────────┤
│ MyBatis Interceptor │ 拦截 SQL 执行            │
├─────────────────────┼──────────────────────────┤
│ 反射                │ 获取 Mapper 方法上的注解 │
├─────────────────────┼──────────────────────────┤
│ JSqlParser          │ 解析和改写 SQL           │
├─────────────────────┼──────────────────────────┤
│ ThreadLocal         │ 获取当前用户上下文       │
├─────────────────────┼──────────────────────────┤
│ 反射修改 BoundSql   │ 替换原始 SQL             │
└─────────────────────┴──────────────────────────┘

  ---
5. 为什么这样设计？

优点：
- ✅ 业务代码无感知（只需加注解）
- ✅ SQL 改写自动化（无需手动拼接）
- ✅ 统一管理（所有数据权限逻辑集中在拦截器）
- ✅ 灵活（支持多表、复杂查询）

缺点：
- ❌ 依赖反射和 SQL 解析（有性能开销）
- ❌ 复杂 SQL 可能解析失败（需要测试）

  ---
理解了吗？需要我继续讲解某个具体步骤吗？

✻ Baked for 32s

> mybatis拦截器我不太理解，包括这个在整个mybatis执行的阶段是什么样的呀啥的可以帮我讲解一下嘛

● 我来详细讲解 MyBatis 拦截器的原理和执行阶段。

一、MyBatis 完整执行流程

1. 应用层调用
   ↓
   userMapper.selectUserList(query)

2. 代理层（JDK 动态代理）
   ↓
   MapperProxy 拦截方法调用

3. 会话层
   ↓
   SqlSession.selectList()

4. 执行器层 ⭐ 【拦截器主要作用点】
   ↓
   Executor.query()
5. 语句处理层 ⭐ 【拦截器可选作用点】
   ↓
   StatementHandler.prepare()
   StatementHandler.parameterize()
6. 参数处理层 ⭐ 【拦截器可选作用点】
   ↓
   ParameterHandler.setParameters()

7. 结果处理层 ⭐ 【拦截器可选作用点】
   ↓
   ResultSetHandler.handleResultSets()

8. JDBC 层
   ↓
   PreparedStatement.execute()

9. 数据库执行
   ↓
   返回结果集

10. 结果映射
    ↓
    返回 Java 对象

  ---
二、MyBatis 四大核心对象（可拦截）

MyBatis 允许拦截以下 4 个核心对象：

1. Executor（执行器）

职责： 负责整个 SQL 执行流程的调度

可拦截的方法：
// 查询方法
query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)

// 增删改方法
update(MappedStatement ms, Object parameter)

// 事务提交
commit(boolean required)

// 事务回滚
rollback(boolean required)

使用场景：
- ✅ 数据权限过滤（在 SQL 执行前修改）
- ✅ 分页插件（修改 SQL 添加 LIMIT）
- ✅ SQL 日志记录
- ✅ 慢查询监控

  ---
2. StatementHandler（语句处理器）

职责： 负责 SQL 语句的准备和执行

可拦截的方法：
// 准备语句（创建 Statement 对象）
prepare(Connection connection, Integer transactionTimeout)

// 设置参数
parameterize(Statement statement)

// 执行查询
query(Statement statement, ResultHandler resultHandler)

// 执行更新
update(Statement statement)

使用场景：
- ✅ SQL 改写（比 Executor 更底层）
- ✅ 动态修改超时时间
- ✅ SQL 防火墙（检测危险 SQL）

  ---
3. ParameterHandler（参数处理器）

职责： 负责将 Java 对象参数设置到 PreparedStatement

可拦截的方法：
// 设置参数
setParameters(PreparedStatement ps)

// 获取参数对象
getParameterObject()

使用场景：
- ✅ 参数加密（敏感字段自动加密）
- ✅ 参数校验
- ✅ 参数日志记录

  ---
4. ResultSetHandler（结果集处理器）

职责： 负责将 JDBC ResultSet 映射为 Java 对象

可拦截的方法：
// 处理结果集
handleResultSets(Statement stmt)

// 处理输出参数（存储过程）
handleOutputParameters(CallableStatement cs)

使用场景：
- ✅ 结果脱敏（手机号、身份证自动打码）
- ✅ 结果解密
- ✅ 结果缓存

  ---
三、拦截器的实现原理

1. 拦截器接口

public interface Interceptor {

      // 拦截逻辑（核心方法）
      Object intercept(Invocation invocation) throws Throwable;

      // 生成代理对象（可选，一般使用默认实现）
      default Object plugin(Object target) {
          return Plugin.wrap(target, this);
      }

      // 设置属性（可选，用于配置参数）
      default void setProperties(Properties properties) {
      }
}

  ---
2. 拦截器注解

@Intercepts({
@Signature(
type = Executor.class,              // 拦截哪个对象
method = "query",                   // 拦截哪个方法
args = {                            // 方法参数类型（用于匹配重载方法）
MappedStatement.class,
Object.class,
RowBounds.class,
ResultHandler.class
}
)
})
public class MyInterceptor implements Interceptor {
// ...
}

关键点：
- type：指定拦截的核心对象（Executor/StatementHandler/ParameterHandler/ResultSetHandler）
- method：指定拦截的方法名
- args：方法参数类型数组（必须完全匹配）

  ---
3. 拦截器执行流程

@Override
public Object intercept(Invocation invocation) throws Throwable {

      // 1. 前置处理（在目标方法执行前）
      System.out.println("方法执行前...");

      // 2. 获取拦截的参数
      Object[] args = invocation.getArgs();
      MappedStatement ms = (MappedStatement) args[0];
      Object parameter = args[1];

      // 3. 修改参数或执行其他逻辑
      // ...

      // 4. 执行目标方法
      Object result = invocation.proceed();  // 继续执行原方法

      // 5. 后置处理（在目标方法执行后）
      System.out.println("方法执行后...");

      // 6. 返回结果（可以修改返回值）
      return result;
}

  ---
四、拦截器的工作原理（动态代理）

MyBatis 使用 JDK 动态代理 实现拦截器：

原始对象（Executor）↓
被拦截器包装
↓
代理对象（Proxy）
↓
调用方法时
↓
先执行拦截器的 intercept() 方法
↓
在 intercept() 中调用 invocation.proceed()
↓
执行原始对象的方法

代码示例：

// MyBatis 内部实现（简化版）
public class Plugin implements InvocationHandler {

      private final Object target;           // 原始对象
      private final Interceptor interceptor; // 拦截器

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          // 判断是否需要拦截
          if (shouldIntercept(method)) {
              // 创建 Invocation 对象
              Invocation invocation = new Invocation(target, method, args);
              // 执行拦截器
              return interceptor.intercept(invocation);
          } else {
              // 不拦截，直接执行原方法
              return method.invoke(target, args);
          }
      }
}

  ---
五、数据权限拦截器的执行时机

用户调用：userMapper.selectUserList(query)
↓
MapperProxy 代理
↓
SqlSession.selectList()
↓
【拦截点】Executor.query() ⭐ 数据权限拦截器在这里介入
↓
拦截器执行：
1. 获取原始 SQL
2. 检查 @DataScope 注解
3. 获取当前用户数据权限
4. 改写 SQL（添加 WHERE 条件）
5. 调用 invocation.proceed() 继续执行
↓
StatementHandler.prepare()（创建 PreparedStatement）
↓
ParameterHandler.setParameters()（设置参数）
↓
PreparedStatement.execute()（执行改写后的 SQL）
↓
ResultSetHandler.handleResultSets()（处理结果）
↓
返回过滤后的数据

  ---
六、为什么选择拦截 Executor？

┌──────────────────┬──────────────────────────────────┬──────────────────────────┬───────────────────┐
│     拦截对象     │               优点               │           缺点           │     适用场景      │
├──────────────────┼──────────────────────────────────┼──────────────────────────┼───────────────────┤
│ Executor         │ SQL 还未生成 Statement，容易修改 │ 需要解析 SQL 字符串      │ ✅ 数据权限、分页 │
├──────────────────┼──────────────────────────────────┼──────────────────────────┼───────────────────┤
│ StatementHandler │ 更底层，可以修改 Statement       │ SQL 已经准备好，修改复杂 │ SQL 防火墙        │
├──────────────────┼──────────────────────────────────┼──────────────────────────┼───────────────────┤
│ ParameterHandler │ 可以修改参数                     │ 无法修改 SQL 结构        │ 参数加密          │
├──────────────────┼──────────────────────────────────┼──────────────────────────┼───────────────────┤
│ ResultSetHandler │ 可以修改返回结果                 │ SQL 已执行，无法减少查询 │ 结果脱敏          │
└──────────────────┴──────────────────────────────────┴──────────────────────────┴───────────────────┘

数据权限选择 Executor 的原因：
- ✅ 可以直接修改 SQL 字符串（添加 WHERE 条件）
- ✅ 在 SQL 执行前介入，避免查询无关数据
- ✅ 性能最优（数据库层面过滤）

  ---
需要我继续讲解拦截器的具体实现代码吗？


> 也就是说，还可以在mybatis的这个机制下进行慢SQL记录？和mysql自身的slowlog比，有什么区别呀

● 对，完全可以用 MyBatis 拦截器实现慢 SQL 记录。两者有明显区别：

MyBatis 拦截器 vs MySQL Slow Log

┌──────────┬────────────────────────────────────┬───────────────────────────────────────┐
│ 对比维度 │        MyBatis 拦截器慢 SQL        │            MySQL Slow Log             │
├──────────┼────────────────────────────────────┼───────────────────────────────────────┤
│ 记录位置 │ 应用层（Java 进程）                │ 数据库层（MySQL 服务器）              │
├──────────┼────────────────────────────────────┼───────────────────────────────────────┤
│ 记录内容 │ SQL + 参数 + 执行时间 + 业务上下文 │ SQL + 执行时间 + 锁等待时间           │
├──────────┼────────────────────────────────────┼───────────────────────────────────────┤
│ 阈值配置 │ 代码中灵活配置（可动态调整）       │ MySQL 配置文件（需重启或 SET GLOBAL） │
├──────────┼────────────────────────────────────┼───────────────────────────────────────┤
│ 性能影响 │ 轻微（仅拦截应用发出的 SQL）       │ 较大（记录所有慢查询到文件）          │
├──────────┼────────────────────────────────────┼───────────────────────────────────────┤
│ 业务关联 │ ✅ 可记录用户、接口、Mapper 方法   │ ❌ 无法关联业务信息                   │
├──────────┼────────────────────────────────────┼───────────────────────────────────────┤
│ 参数展示 │ ✅ 可展示实际参数值                │ ❌ 只有占位符 ?                       │
├──────────┼────────────────────────────────────┼───────────────────────────────────────┤
│ 实时告警 │ ✅ 可直接发送到监控系统            │ ❌ 需要额外工具解析日志               │
├──────────┼────────────────────────────────────┼───────────────────────────────────────┤
│ 多数据源 │ ✅ 可区分不同数据源                │ ❌ 每个 MySQL 实例独立记录            │
└──────────┴────────────────────────────────────┴───────────────────────────────────────┘

  ---
具体区别示例

场景：查询用户列表耗时 3 秒

MySQL Slow Log 记录：
# Time: 2026-03-27T03:30:51.123456Z
# User@Host: atlas[atlas] @ localhost []
# Query_time: 3.125000  Lock_time: 0.000123 Rows_sent: 1000  Rows_examined: 50000
SELECT * FROM sys_user WHERE dept_id = ? AND status = ?;

问题：
- ❌ 不知道 ? 的实际值是什么
- ❌ 不知道是哪个接口调用的
- ❌ 不知道是哪个用户触发的
- ❌ 需要手动分析日志文件

  ---
MyBatis 拦截器记录：
{
"sql": "SELECT * FROM sys_user WHERE dept_id = 10 AND status = 1",
"executionTime": 3125,
"mapper": "com.atlas.system.mapper.SysUserMapper.selectUserList",
"userId": 100,
"userName": "张三",
"requestUri": "/system/user/list",
"params": {
"deptId": 10,
"status": 1
},
"timestamp": "2026-03-27T03:30:51.123Z"
}

优势：
- ✅ 参数值清晰可见
- ✅ 知道是 /system/user/list 接口
- ✅ 知道是用户"张三"触发的
- ✅ 可直接发送到 ELK/Prometheus 告警

  ---
MyBatis 慢 SQL 拦截器实现

@Intercepts({
@Signature(
type = Executor.class,
method = "query",
args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
),
@Signature(
type = Executor.class,
method = "update",
args = {MappedStatement.class, Object.class}
)
})
@Component
@Slf4j
public class SlowSqlInterceptor implements Interceptor {

      // 慢 SQL 阈值（毫秒）
      private static final long SLOW_SQL_THRESHOLD = 1000;

      @Override
      public Object intercept(Invocation invocation) throws Throwable {
          long startTime = System.currentTimeMillis();

          // 执行 SQL
          Object result = invocation.proceed();

          long endTime = System.currentTimeMillis();
          long executionTime = endTime - startTime;

          // 超过阈值，记录慢 SQL
          if (executionTime > SLOW_SQL_THRESHOLD) {
              logSlowSql(invocation, executionTime);
          }

          return result;
      }

      private void logSlowSql(Invocation invocation, long executionTime) {
          MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
          Object parameter = invocation.getArgs()[1];
          BoundSql boundSql = ms.getBoundSql(parameter);

          String sql = boundSql.getSql();
          String mapperId = ms.getId();

          // 获取当前用户信息（可选）
          LoginUser user = SecurityContextHolder.getLoginUser();

          log.warn("慢SQL告警 | 耗时: {}ms | Mapper: {} | SQL: {} | 参数: {} | 用户: {}",
              executionTime, mapperId, sql, parameter,
              user != null ? user.getUsername() : "未登录");

          // 可选：发送到监控系统
          // monitorService.reportSlowSql(sql, executionTime, mapperId);
      }
}

  ---
使用场景建议

使用 MyBatis 拦截器：

- ✅ 需要关联业务上下文（用户、接口、参数）
- ✅ 需要实时告警和监控
- ✅ 需要动态调整阈值
- ✅ 微服务架构（多数据源统一监控）

使用 MySQL Slow Log：

- ✅ 需要分析数据库层面的性能（锁等待、扫描行数）
- ✅ DBA 进行数据库优化
- ✅ 排查数据库本身的问题
- ✅ 需要记录所有客户端的慢查询（不仅是应用）

  ---
最佳实践：两者结合

MyBatis 拦截器（应用层）↓
记录业务相关的慢 SQL
发送告警到监控系统
+
MySQL Slow Log（数据库层）
↓
DBA 分析数据库性能
优化索引和查询计划

典型流程：
1. MyBatis 拦截器发现慢 SQL 告警
2. 开发人员查看业务上下文（哪个接口、哪个用户）
3. DBA 查看 MySQL Slow Log 分析执行计划
4. 优化索引或 SQL 结构

这样理解了吗？







