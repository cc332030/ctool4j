# ctool4j-auth-pom

> 认证组件：JWT 校验与签发、token 读写、会话服务与 Spring Security 认证过滤器。

## 简介

`ctool4j-auth-pom` 面向 Spring Boot / Spring Security 应用，提供认证能力的组件化封装，分两个子模块：

- `ctool4j-auth-base`：与具体框架无关的认证基础能力（JWT 校验/签发、认证与会话配置）。
- `ctool4j-auth-spring`：Spring Security 集成（认证过滤器基类、基于 Redis 的会话服务）。

## 功能特性

- **JWT 校验与签发**：`CAuthUtils` 以配置密钥校验 jwt 并解析其中的 token；为 jwt body 生成 jwt 并写入响应 `Authorization` 头
- **token 读写**：请求/响应头与请求属性的 token 读写由 `ctool4j-web` 的 `CTokenUtils` 提供
- **会话服务**：`CAbstractSessionService` 基于 Redis 提供会话存取、按 jwt 取会话、当前会话获取；当前会话缺失（未授权）时 `get()`/`check()` 抛 `CUnauthorizedException`，由 `ctool4j-web` 的 `CUnauthorizedExceptionHandler` 统一转为响应体业务码 401（HTTP 状态保持 200，与模块内其余处理器一致；见 [未授权统一返回业务码 401 设计文档](../../../doc/design/web/unauthorized-401.adoc)）
- **认证过滤器**：`CAbstractAuthFilter` 解析 token → 加载会话 → 构造 Spring Security 认证信息写入安全上下文；解析失败静默放行，交由后续授权规则拦截
- **认证过滤器继承链**（自下而上，业务继承链末端）：`CAbstractWebAuthFilter`（`ctool4j-web`，类型契约）→ `CAbstractBaseAuthFilter`（auth-base，会话加载与过滤器骨架，不依赖 Spring Security）→ `CAbstractAuthFilter`（auth-spring，Security 认证构造）
- **认证装配基类**：`CAbstractAuthBaseConfiguration`（auth-base）提供 mock 会话配置落点；`CAbstractAuthConfiguration`（auth-spring）在其上追加默认认证过滤器 bean，业务子类只需实现 `isAuthAnonymous`（二者按是否引入 Spring Security 择一继承）

## 模块结构

| 子模块 | 定位 |
|--------|------|
| `ctool4j-auth-base` | 认证基础：`CAuthUtils`、`CAuthConfig`、`CSessionConfig`、`CAbstractAuthBaseConfiguration`、`ICJwtInfo`、`ICSession`、`CAbstractBaseAuthFilter`、`CAbstractBaseSessionService`、`CSessionUtils`、`CAbstractSessionMockConfig` |
| `ctool4j-auth-spring` | Spring Security 集成：`CAbstractAuthConfiguration`、`CAbstractAuthFilter`、`CAbstractSessionService`、`ICSecuritySession` |

## 核心类

| 类 | 模块 | 类型 | 职责 |
|----|------|------|------|
| `CAuthUtils` | auth-base | 工具类 | 以配置密钥校验 jwt 并解析 token；生成 jwt 并写入响应头 |
| `CAuthConfig` | auth-base | 配置类 | 认证配置（`jwtSecret`） |
| `CSessionConfig` | auth-base | 配置类 | 会话配置（`expire`） |
| `CAbstractAuthBaseConfiguration` | auth-base | 装配基类 | 认证模块默认装配：业务未自建 `CAbstractSessionMockConfig` 时提供关闭态默认 bean；业务子类加 `@Configuration` 继承其 `@Bean` 方法并固定会话类型 |
| `CAbstractAuthConfiguration` | auth-spring | 装配基类 | 在 `CAbstractAuthBaseConfiguration` 之上追加默认认证过滤器（`cAuthFilter`）与会话服务（`cSessionService`）bean：业务子类实现 `isAuthAnonymous` 即得；业务自建同类型 bean 时不覆盖 |
| `ICJwtInfo` | auth-base | 接口 | jwt body（载荷）标记接口，实现类字段即载荷内容 |
| `CAbstractBaseAuthFilter` | auth-base | 过滤器基类 | 认证过滤器公共部分：过滤器骨架、mock 会话加载、加载会话；不依赖 Spring Security |
| `CSessionUtils` | auth-base | 工具类 | 会话读写静态门面（方法名不带 `Session` 后缀、方法集合与顺序对齐 `CAbstractBaseSessionService`；**当前全部方法均为透传**）：`get(token)` / `load(request)` / `getDefaultNull()` / `getByJwt(jwt)` 读取（查不到返回 null）、`get()` / `check()` 取当前会话（无会话抛 `CUnauthorizedException`）、`save(token, session)` / `remove(token)` 写入与删除；泛型按调用方声明自动适配 |
| `CAbstractAuthFilter` | auth-spring | 过滤器基类 | 业务直接继承：构造 Security 认证信息（普通会话按匿名判定、mock 会话无条件已认证） |
| `CAbstractSessionService` | auth-spring | 服务基类 | 基于 Redis 的会话存取、按 jwt 取会话、当前会话获取 |

## 依赖

| 子模块 | 依赖 |
|--------|------|
| `ctool4j-auth-base` | `ctool4j-web`（JWT 编解码 `CJwtUtils`、token 读写 `CTokenUtils`）、`ctool4j-cache`（会话存储） |
| `ctool4j-auth-spring` | `ctool4j-auth-base`、`ctool4j-spring-security`（安全上下文与工具） |

## 使用与配置（引用方）

引入坐标、用法示例、配置项、模块选型、误用点等**面向引用方**的内容：见使用文档 [`doc/use/auth.adoc`](../../../doc/use/auth.adoc)（整体开放为静态服务），本 README 不再重复（同一事实两个真源必然漂移）。
