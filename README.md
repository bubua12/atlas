<h1 align="center">Atlas</h1>
<p align="center">企业级微服务开发平台脚手架</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-21-green.svg" alt="JDK 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.3-blue.svg" alt="Spring Boot 3.2.3"/>
  <img src="https://img.shields.io/badge/Spring%20Cloud-2023.0.0-blue.svg" alt="Spring Cloud 2023.0.0"/>
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.5-orange.svg" alt="MyBatis-Plus 3.5.5"/>
  <img src="https://img.shields.io/badge/license-MIT-green.svg" alt="MIT License"/>
</p>

---

## 简介

Atlas 是一个基于 Spring Boot 3 + Spring Cloud + JDK 21 构建的微服务开发平台骨架，旨在为企业级应用提供开箱即用的基础能力，包括用户认证、权限管理、系统监控、代码生成等核心功能。

项目采用模块化设计，公共能力按职责拆分为独立子模块，业务服务通过 Feign API 模块解耦，适合作为中后台管理系统的技术底座。

## 技术栈

| 分类        | 技术                   | 版本           |
|-----------|----------------------|--------------|
| 核心框架      | Spring Boot          | 3.2.3        |
| 微服务       | Spring Cloud         | 2023.0.0     |
| 微服务（阿里巴巴） | Spring Cloud Alibaba | 2023.0.1.0   |
| ORM       | MyBatis-Plus         | 3.5.5        |
| 数据库       | MySQL                | 8.x          |
| 缓存        | Redis + Redisson     | 7.x / 3.27.0 |
| 注册/配置中心   | Nacos                | 2.x          |
| 网关        | Spring Cloud Gateway | -            |
| 认证        | JWT (jjwt)           | 0.12.5       |
| 连接池       | Druid                | 1.2.21       |
| 工具库       | Hutool               | 5.8.25       |
| 构建工具      | Maven                | 3.9+         |
| JDK       | OpenJDK              | 21           |

## 项目结构

```
atlas
├── atlas-dependencies              BOM 依赖版本统一管理
│
├── atlas-common                    公共模块
│   ├── atlas-common-core           ├── 核心：统一响应体、基础实体、异常、常量
│   ├── atlas-common-web            ├── Web：全局异常处理器、Jackson 配置
│   ├── atlas-common-redis          ├── Redis：缓存封装、分布式锁（Redisson）
│   ├── atlas-common-mybatis        ├── ORM：MyBatis-Plus 分页、自动填充
│   ├── atlas-common-security       ├── 安全：权限注解、SecurityContext、LoginUser
│   └── atlas-common-log            └── 日志：操作日志注解 + AOP 切面
│
├── atlas-gateway                   API 网关（Spring Cloud Gateway）
│   ├── AuthFilter                  ├── Token 校验过滤器
│   └── CorsConfig                  └── 跨域配置
│
├── atlas-auth                      认证授权服务
│   ├── AuthController              ├── 登录 / 登出 / Token 刷新
│   ├── AuthServiceImpl             ├── JWT + Redis 认证实现
│   └── JwtUtils                    └── JWT 工具类
│
├── atlas-system                    系统管理服务
│   ├── atlas-system-api            ├── Feign 接口 + DTO（UserDTO、RoleDTO）
│   └── atlas-system-biz            └── 用户 / 角色 / 菜单 / 部门 / 字典 CRUD
│
├── atlas-infra                     基础设施服务
│   ├── atlas-infra-api             ├── Feign 接口（RemoteFileService）
│   └── atlas-infra-biz             └── 文件上传 / 代码生成
│
└── atlas-monitor                   监控服务
    ├── Spring Boot Admin           ├── 服务监控面板
    ├── ServerController            ├── 服务器资源信息
    └── OnlineUserController        └── 在线用户管理
```

## 架构概览

```
                              ┌──────────────┐
                              │   Frontend   │
                              │  (Vue3+Vite) │
                              └──────┬───────┘
                                     │
                              ┌──────▼───────┐
                              │   Gateway    │
                              │    :8080     │
                              └──────┬───────┘
                                     │
                 ┌───────────────────┼───────────────────┐
                 │                   │                    │
          ┌──────▼──────┐    ┌──────▼──────┐    ┌───────▼──────┐
          │    Auth     │    │   System    │    │    Infra     │
          │   :9100     │    │   :9200     │    │    :9300     │
          └──────┬──────┘    └──────┬──────┘    └───────┬──────┘
                 │                  │                    │
                 └──────────────────┼────────────────────┘
                                    │
                 ┌──────────────────┼──────────────────┐
                 │                  │                   │
          ┌──────▼──────┐   ┌──────▼──────┐   ┌───────▼──────┐
          │    Nacos     │   │    MySQL    │   │    Redis     │
          │    :8848     │   │    :3306    │   │    :6379     │
          └─────────────┘   └─────────────┘   └──────────────┘
```

## 服务端口

| 服务            | 端口   | 说明          |
|---------------|------|-------------|
| atlas-gateway | 8080 | API 网关，统一入口 |
| atlas-auth    | 9100 | 认证授权服务      |
| atlas-system  | 9200 | 系统管理服务      |
| atlas-infra   | 9300 | 基础设施服务      |
| atlas-monitor | 9400 | 监控服务        |

## 快速开始

### 环境准备

确保本地已安装以下服务：

- JDK 21+
- Maven 3.9+
- MySQL 8.x
- Redis 7.x
- Nacos 2.x

### 1. 克隆项目

```bash
git clone https://github.com/your-org/atlas.git
cd atlas
```

### 2. 初始化数据库

创建数据库 `atlas`，字符集 `utf8mb4`：

```sql
CREATE DATABASE atlas DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 3. 启动基础设施

```bash
# 启动 Nacos（单机模式）
sh nacos/bin/startup.sh -m standalone

# 启动 Redis
redis-server

# 启动 MySQL
# 确保 MySQL 服务已运行
```

### 4. 构建项目

```bash
mvn clean install -DskipTests
```

### 5. 按顺序启动服务

```bash
# 1. 网关
java -jar atlas-gateway/target/atlas-gateway-1.0.0.jar

# 2. 认证服务
java -jar atlas-auth/target/atlas-auth-1.0.0.jar

# 3. 系统服务
java -jar atlas-system/atlas-system-biz/target/atlas-system-biz-1.0.0.jar

# 4. 基础设施服务
java -jar atlas-infra/atlas-infra-biz/target/atlas-infra-biz-1.0.0.jar

# 5. 监控服务
java -jar atlas-monitor/target/atlas-monitor-1.0.0.jar
```

或在 IDEA 中分别运行各服务的 Application 启动类。

## 网关路由

所有请求通过网关 `http://localhost:8080` 统一转发：

| 路径前缀          | 转发目标          | 说明     |
|---------------|---------------|--------|
| `/auth/**`    | atlas-auth    | 认证相关接口 |
| `/system/**`  | atlas-system  | 系统管理接口 |
| `/infra/**`   | atlas-infra   | 基础设施接口 |
| `/monitor/**` | atlas-monitor | 监控相关接口 |

## 核心接口

### 认证

```
POST /auth/login          # 登录，获取 Token
POST /auth/logout         # 登出，销毁 Token
POST /auth/refresh        # 刷新 Token
```

### 系统管理

```
GET    /system/user       # 用户列表（分页）
GET    /system/user/{id}  # 用户详情
POST   /system/user       # 创建用户
PUT    /system/user       # 更新用户
DELETE /system/user/{id}  # 删除用户

GET    /system/role       # 角色管理
GET    /system/menu       # 菜单管理
GET    /system/dept       # 部门管理
GET    /system/dict       # 字典管理
```

### 基础设施

```
POST   /infra/file/upload           # 文件上传
DELETE /infra/file/{fileId}         # 文件删除
GET    /infra/codegen/tables        # 数据表列表
POST   /infra/codegen/generate/{t}  # 代码生成
```

### 监控

```
GET    /monitor/server/info    # 服务器信息
GET    /monitor/online         # 在线用户列表
DELETE /monitor/online/{id}    # 强制下线
```

## 模块依赖关系

```
atlas-common-core ◄─── atlas-common-web
                  ◄─── atlas-common-redis
                  ◄─── atlas-common-mybatis
                  ◄─── atlas-common-security ◄── atlas-common-redis
                  ◄─── atlas-common-log

atlas-gateway     ◄─── atlas-common-core

atlas-auth        ◄─── atlas-common-web
                  ◄─── atlas-common-redis
                  ◄─── atlas-common-security
                  ◄─── atlas-system-api

atlas-system-biz  ◄─── atlas-system-api
                  ◄─── atlas-common-web / redis / mybatis / security / log

atlas-infra-biz   ◄─── atlas-infra-api
                  ◄─── atlas-common-web / redis / mybatis / security / log

atlas-monitor     ◄─── atlas-common-web / redis / security / log
```

## 配置说明

各服务的配置文件位于 `src/main/resources/application.yml`，主要配置项：

| 配置项                                        | 默认值                                 | 说明               |
|--------------------------------------------|-------------------------------------|------------------|
| `spring.cloud.nacos.discovery.server-addr` | `127.0.0.1:8848`                    | Nacos 地址         |
| `spring.datasource.url`                    | `jdbc:mysql://127.0.0.1:3306/atlas` | 数据库连接            |
| `spring.data.redis.host`                   | `127.0.0.1`                         | Redis 地址         |
| `atlas.jwt.secret`                         | `atlas-jwt-secret-key-...`          | JWT 密钥（生产环境务必修改） |
| `atlas.jwt.expiration`                     | `7200`                              | Token 有效期（秒）     |

## 开发规范

- 统一响应体：所有接口返回 `R<T>` 格式，包含 `code`、`msg`、`data`
- 服务间调用：通过 `xxx-api` 模块的 Feign 接口，不直接依赖实现模块
- 操作日志：在 Controller 方法上添加 `@OperLog` 注解自动记录
- 权限控制：使用 `@RequiresPermission` 注解标记需要权限的接口
- 分页查询：统一使用 `PageQuery` 接收分页参数

## License

[MIT](LICENSE)
