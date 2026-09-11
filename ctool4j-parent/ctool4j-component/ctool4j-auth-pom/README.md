# ctool4j-auth-pom

> 认证组件：JWT 校验与签发、token 读写、会话服务与 Spring Security 认证过滤器。

## 简介

`ctool4j-auth-pom` 面向 Spring Boot / Spring Security 应用，提供认证能力的组件化封装，分两个子模块：

- `ctool4j-auth-base`：与具体框架无关的认证基础能力（JWT 校验/签发、认证与会话配置）。
- `ctool4j-auth-spring`：Spring Security 集成（认证过滤器基类、基于 Redis 的会话服务）。

## 功能特性

- **JWT 校验与签发**：`CAuthUtils` 以配置密钥校验 jwt 并解析其中的 token；为 jwt body 生成 jwt 并写入响应 `Authorization` 头
- **token 读写**：请求/响应头与请求属性的 token 读写由 `ctool4j-web` 的 `CTokenUtils` 提供
- **会话服务**：`CAbstractSessionService` 基于 Redis 提供会话存取、按 jwt 取会话、当前会话获取
- **认证过滤器**：`CAbstractAuthFilter` 解析 jwt → 加载会话 → 构造 Spring Security 认证信息写入安全上下文；解析失败静默放行，交由后续授权规则拦截

## 模块结构

| 子模块 | 定位 |
|--------|------|
| `ctool4j-auth-base` | 认证基础：`CAuthUtils`、`CAuthConfig`、`CSessionConfig`、`ICJwtInfo`、`ICSession` |
| `ctool4j-auth-spring` | Spring Security 集成：`CAbstractAuthFilter`、`CAbstractSessionService`、`ICSecuritySession`、`CAbstractSpringSecurityMockSessionConfig` |

## 核心类

| 类 | 模块 | 类型 | 职责 |
|----|------|------|------|
| `CAuthUtils` | auth-base | 工具类 | 以配置密钥校验 jwt 并解析 token；生成 jwt 并写入响应头 |
| `CAuthConfig` | auth-base | 配置类 | 认证配置（`jwtSecret`） |
| `CSessionConfig` | auth-base | 配置类 | 会话配置（`expire`） |
| `ICJwtInfo` | auth-base | 接口 | jwt body（载荷）标记接口，实现类字段即载荷内容 |
| `CAbstractAuthFilter` | auth-spring | 过滤器基类 | 认证过滤器：解析 jwt、加载会话、写入 Security 认证信息 |
| `CAbstractSessionService` | auth-spring | 服务基类 | 基于 Redis 的会话存取、按 jwt 取会话、当前会话获取 |

## 使用示例

```java
// 校验 jwt 并解析其中携带的 token（失败静默返回 null）
String token = CAuthUtils.getTokenByJwt(jwt);

// 以 jwt body 生成 jwt 并写入响应 Authorization 头
CAuthUtils.setJwt(jwtInfo);

// 按 jwt 读取会话
SESSION session = sessionService.getSessionByJwt(jwt);
```

## 配置项

| 配置 | 说明 |
|------|------|
| `CAuthConfig#jwtSecret` | jwt 校验/签发所用密钥 |
| `CSessionConfig#expire` | 会话过期时间 |

## 依赖

| 子模块 | 依赖 |
|--------|------|
| `ctool4j-auth-base` | `ctool4j-web`（JWT 编解码 `CJwtUtils`、token 读写 `CTokenUtils`） |
| `ctool4j-auth-spring` | `ctool4j-auth-base`、`ctool4j-cache`、`ctool4j-spring-security` |
