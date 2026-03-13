# 操作日志功能使用说明

## 功能概述
操作日志功能已实现数据库持久化，可自动记录操作人、操作IP、请求参数、返回结果、执行耗时等信息。

## 使用方法

在 Controller 方法上添加 `@OperLog` 注解：

```java
@OperLog(title = "用户管理", businessType = "新增")
@PostMapping("/add")
public R<Void> addUser(@RequestBody SysUser user) {
    // 业务逻辑
}
```

## 数据库表

执行 SQL 文件创建表：
```bash
mysql -u root -p atlas < atlas-common/atlas-common-log/src/main/resources/sql/05_sys_oper_log.sql
```

## 记录字段

- **operId**: 日志主键（自增）
- **title**: 操作标题
- **businessType**: 业务类型（新增、修改、删除等）
- **method**: 完整方法名
- **requestMethod**: 请求方式（GET/POST/PUT/DELETE）
- **operName**: 操作人用户名（从 SecurityContextHolder 获取）
- **operIp**: 操作IP地址（支持代理和负载均衡）
- **operParam**: 请求参数（JSON格式，最大2000字符）
- **jsonResult**: 返回结果（JSON格式，最大2000字符）
- **status**: 操作状态（0成功 1失败）
- **errorMsg**: 错误消息（失败时记录）
- **operTime**: 操作时间
- **costTime**: 执行耗时（毫秒）

## 技术实现

- 使用 AOP 切面自动拦截
- 异步线程池处理，不阻塞主业务
- 自动获取真实IP（处理 X-Forwarded-For 等代理场景）
- 自动序列化请求参数和返回结果
- 超长内容自动截断
