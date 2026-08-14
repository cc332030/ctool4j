# ctool4j-web

> Web MVC 通用能力封装：全局异常统一处理、跨域支持、JWT 与认证工具、请求响应体增强、HTTP 请求日志与 traceId 透传。

## 简介

`ctool4j-web` 面向 Spring Boot Web 应用，开箱即用地提供异常统一返回、CORS 跨域、认证辅助、HTTP 请求日志与 traceId 透传等能力，规范 Web 层的错误处理与安全基础。

## 功能特性

- **全局异常处理**：内置多类异常处理器（业务异常、参数校验异常、HTTP 方法不支持、消息不可写、客户端中断、非法参数/状态、兜底 Throwable），通过 `@ConditionalOnMissingExceptionHandler` 支持业务自定义覆盖
- **跨域全面支持**：`CCorsConfig` / `CCorsFilter` / `CCorsInterceptor` / 响应体增强多层方案
- **认证与 JWT**：`CJwtUtils`（JWT 生成/解析）、`CAuthUtils`（认证辅助）
- **请求头枚举**：`CRequestHeaderEnum` 统一请求头名称
- **MVC 配置**：`CWebMvcConfigurer`（拦截器、静态资源配置）、静态资源过滤器 `ResourceFilter`
- **统一错误页**：`CErrorController`
- **请求/响应体增强抽象**：`ICBaseRequestBodyAdvice` / `ICBaseResponseBodyAdvice`（供日志等模块继承）
- **HTTP 请求日志**：`CRequestLogUtils` 构造 / 保存 / 输出 `CRequestLog`（HTTP 格式 dump），`CCommUtils.appendHttpLog` 统一拼接，支持 URI 排除、请求体记录
- **traceId 透传**：`CTraceUtils` 从请求头读取 traceId（无则生成），写入 ThreadLocal + MDC（key = `c-trace-id`），支持 SPI 定制提供者（`ICTraceInfoProvider`）
- **工具类**：`CServletUtils`、`CWebUtils`

## 包结构

| 包 | 用途 |
|----|------|
| `advice` | 请求/响应体增强抽象接口 |
| `configuration` / `configurer` | Web 初始化与 MVC 配置 |
| `exception` | 异常注解、装配条件、各异常处理器 |
| `cors` | 跨域配置 / 过滤器 / 拦截器 / 增强 / 工具 |
| `filter` / `interceptor` | 过滤器与拦截器抽象 |
| `controller` | 统一错误控制器 |
| `util` | 认证、JWT、Servlet、Web 工具 |
| `constant` / `enums` | 资源路径常量、请求头枚举 |

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CCBusinessExceptionHandler` | 处理器 | 业务异常统一返回 |
| `CCExceptionHandler` | 处理器 | 通用异常返回 |
| `CMethodArgumentNotValidExceptionHandler` | 处理器 | 参数校验异常返回 |
| `CThrowableHandler` | 处理器 | 兜底异常返回 |
| `ConditionalOnMissingExceptionHandler` | 注解 | 仅当业务未自定义处理器时装配 |
| `CCorsConfig` / `CCorsFilter` / `CCorsInterceptor` | 配置/过滤器/拦截器 | 跨域支持 |
| `CJwtUtils` | 工具类 | JWT 生成与解析 |
| `CAuthUtils` | 工具类 | 认证信息辅助 |
| `CRequestHeaderEnum` | 枚举 | 请求头名称统一管理 |
| `CErrorController` | 控制器 | 统一错误页 |
| `CWebMvcConfigurer` | 配置 | MVC 拦截器与资源映射 |
| `ICBaseResponseBodyAdvice` | 接口 | 响应体增强抽象（日志模块使用） |
| `CRequestLogUtils` | 工具类 | 请求日志构造 / 保存 / 输出（HTTP 格式 dump） |
| `CRequestLog` | 模型 | 请求日志实体（builder 构造） |
| `CRequestLogConfig` | 配置 | `logging.request-log` 配置项 |
| `CCommUtils` | 工具类 | HTTP 报文拼接（请求行、请求头、body 等） |
| `CTraceUtils` | 工具类 | traceId 生成与传递（ThreadLocal + MDC + SPI） |
| `ICTraceInfo` / `CTraceInfo` | 模型 | traceId 载体接口与默认实现 |
| `ICTraceInfoProvider` | SPI | traceInfo 提供者 SPI |

## 使用示例

```java
// 业务异常（由 CCBusinessExceptionHandler 统一转为 JSON 返回）
throw new CBusinessException("订单不存在");

// JWT 工具
String token = CJwtUtils.createToken(userId);
Long userId = CJwtUtils.parseToken(token);
```

## 配置项

无独立配置前缀；异常处理装配受 `@ConditionalOnMissingExceptionHandler` 控制，业务可自行定义同类型 Bean 覆盖默认行为。

## 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-spring` | Spring 基础设施 |
| `ctool4j-core` | 业务异常、工具 |
| `spring-boot-starter-web` | Web MVC |
| `validation` | 参数校验 |
