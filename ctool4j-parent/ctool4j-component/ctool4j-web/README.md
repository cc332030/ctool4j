# ctool4j-web

> Web MVC 通用能力封装：全局异常统一处理、跨域支持、JWT 与 token 工具、请求响应体增强、HTTP 请求日志与 traceId 透传。

## 简介

`ctool4j-web` 面向 Spring Boot Web 应用，开箱即用地提供异常统一返回、CORS 跨域、JWT 编解码与 token 读写、HTTP 请求日志与 traceId 透传等能力，规范 Web 层的错误处理与安全基础。

## 功能特性

- **全局异常处理**：内置多类异常处理器（业务异常、参数校验异常、请求体缺失/不可读、必填参数缺失、参数类型不匹配、HTTP 方法不支持、消息不可写、客户端中断、非法参数/状态、兜底 Throwable），通过 `@ConditionalOnMissingExceptionHandler` 支持业务自定义覆盖；**处理器优先级（兜底不抢占）**：三档统一由 `CExceptionHandlerOrder` 定义、整体贴近 `Ordered.LOWEST_PRECEDENCE`——具体类型处理器 `CONCRETE`（`LOWEST_PRECEDENCE - 100`）、`CException` 兜底（`CCExceptionHandler`）`C_EXCEPTION_FALLBACK`（`- 50`）、`Throwable` 兜底（`CThrowableHandler`）`THROWABLE_FALLBACK`（`LOWEST_PRECEDENCE`）；Spring 的 `ExceptionHandlerExceptionResolver` 按 advice 顺序取首个能匹配的处理器、不跨 advice 比较异常类型精确度，故具体类型档必须先于兜底档（否则子类异常被截走，如未授权 401 被兜底成 500），而内置处理器整体处于兜底区、不抢占业务方——业务方任一显式 `@Order`（如 `@Order(0)`）都可抢先；覆盖两条路径：声明同类型 `@ExceptionHandler`（条件装配使内置处理器不生效，与顺序无关）、处理子类/超类等非精确类型时用显式 `@Order` 排在对应档位之前；**命中回退**：单个 advice 内无精确类型匹配时 Spring 会按异常 **cause** 回退匹配，故"数字参数传非数字"的 `MethodArgumentTypeMismatchException`（cause 为 `NumberFormatException`）由 `CIllegalArgumentExceptionHandler` 命中，而 cause 非 `IllegalArgumentException` 的类型不匹配则由 `CMethodArgumentTypeMismatchExceptionHandler` 命中
- **跨域全面支持**：`CCorsConfig` / `CCorsFilter` / `CCorsInterceptor` / 响应体增强多层方案
- **JWT 与 token**：`CJwtUtils`（JWT 生成/解析）、`CTokenUtils`（token 前缀、请求头/响应头与请求属性读写）
- **请求头枚举**：`CRequestHeaderEnum` 统一请求头名称
- **MVC 配置**：`CWebMvcConfigurer`（拦截器、静态资源配置）、静态资源过滤器 `CResourceFilter`
- **统一错误页**：`CErrorController`
- **认证过滤器契约**：`CAbstractWebAuthFilter`（抽象基类，仅类型契约）作为认证过滤器继承链顶层，供 auth 模块继承、安全过滤器链按其注入认证过滤器
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
| `CTokenUtils` | 工具类 | token 前缀、请求头/响应头与请求属性读写 |
| `CRequestHeaderEnum` | 枚举 | 请求头名称统一管理 |
| `CErrorController` | 控制器 | 统一错误页 |
| `CAbstractWebAuthFilter` | 过滤器基类 | 认证过滤器类型契约（继承链顶层，auth 模块继承之） |
| `ICFilter` / `CResourceFilter` | 接口/过滤器 | 项目统一过滤器接口与静态资源忽略过滤器 |
| `CWebMvcConfigurer` | 配置 | MVC 拦截器与资源映射 |
| `ICBaseResponseBodyAdvice` | 接口 | 响应体增强抽象（日志模块使用） |
| `CRequestLogUtils` | 工具类 | 请求日志构造 / 保存 / 输出（HTTP 格式 dump） |
| `CRequestLog` | 模型 | 请求日志实体（builder 构造） |
| `CRequestLogConfig` | 配置 | `logging.request-log` 配置项 |
| `CCommUtils` | 工具类 | HTTP 报文拼接（请求行、请求头、body 等） |
| `CTraceUtils` | 工具类 | traceId 生成与传递（ThreadLocal + MDC + SPI） |
| `ICTraceInfo` / `CTraceInfo` | 模型 | traceId 载体接口与默认实现 |
| `ICTraceInfoProvider` | SPI | traceInfo 提供者 SPI |

## 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-spring` | Spring 基础设施 |
| `ctool4j-core` | 业务异常、工具 |
| `spring-boot-starter-web` | Web MVC |
| `validation` | 参数校验 |

## 使用与配置（引用方）

引入坐标、用法示例、配置项、误用点等**面向引用方**的内容：见使用文档 [`doc/use/web.adoc`](../../../doc/use/web.adoc)（整体开放为静态服务），本 README 不再重复（同一事实两个真源必然漂移）。
