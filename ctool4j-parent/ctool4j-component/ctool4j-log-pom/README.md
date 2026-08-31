# ctool4j-log-pom

> 日志聚合模块：请求日志自动埋点（log-base）、logback 适配（logback）、log4j2 适配（log4j）。

## 简介

`ctool4j-log-pom` 是聚合 pom，包含 3 个子模块：`ctool4j-log-base`（请求日志自动埋点与日志级别接口族）、`ctool4j-logback`（MDC 跨线程透传）、`ctool4j-log4j`（MDC 跨线程透传）。

---

## 子模块一：ctool4j-log-base

> HTTP 请求日志自动埋点（拦截器 + advice）。

### 功能特性

- **自动埋点**：`CRequestLogHandlerInterceptor`（拦截器）+ `CLogRequestBodyAdvice` / `CLogResponseBodyAdvice`（ControllerAdvice）自动收集请求/响应日志
- **日志级别接口族**：`ICLogLevel` 及 Debug/Info/Error/Warn/Trace 子接口（按场景输出不同级别）
- **MDC 上下文存储基类**：`CMdc`（基于 TTL 的 MDC 存储，供各日志后端 MDC 适配器继承）

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CRequestLogHandlerInterceptor` | 拦截器 | 请求前后埋点、慢请求告警、清理 ThreadLocal |
| `CLogRequestBodyAdvice` | advice | 读取请求体写入请求日志 |
| `CLogResponseBodyAdvice` | advice | 响应写出前写请求日志 |
| `ICLogLevel` 族 | 接口 | 日志级别接口族 |
| `CMdc` | 基类 | TTL MDC 存储基类 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` / `ctool4j-definition` | 核心工具 |
| `ctool4j-spring` | 请求工具、拦截器/advice 基类 |
| `ctool4j-web` | Web 能力 |

---

## 子模块二：ctool4j-logback

> logback 后端适配。

### 功能特性

- **MDC 跨线程透传**：`CMdcLogback` 实现 slf4j `MDCAdapter`（继承 `CMdc`），用 TTL ThreadLocal 承载 MDC，配合线程池使用实现 traceId 跨线程传递

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CMdcLogback` | 适配器 | slf4j MDCAdapter 适配器（继承 CMdc） |

### 依赖（传递到使用项目）

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` | 核心工具 |
| `ctool4j-spring` | 事件监听、代理 |
| `spring-boot-starter-logging` | logback 日志后端（compile，覆盖父级 provided，传递到使用项目） |

---

## 子模块三：ctool4j-log4j

> log4j2 后端适配。

### 功能特性

- **MDC 跨线程透传**：`CMdcLog4j` 实现 log4j2 `ThreadContextMap`（继承 `CMdc`），用 TTL ThreadLocal 承载 MDC，配合线程池使用实现 traceId 跨线程传递

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CMdcLog4j` | 适配器 | log4j2 ThreadContextMap 适配器（继承 CMdc） |

### 依赖（传递到使用项目）

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` | 核心工具 |
| `ctool4j-spring` | 事件监听、代理 |
| `spring-boot-starter-logging` | 覆盖父级 provided 并排除 logback / log4j-to-slf4j，避免与 log4j2 冲突 |
| `spring-boot-starter-log4j2` | log4j2 日志后端（compile，传递到使用项目） |

---

## 模块选择建议

| 日志后端 | 引入模块 |
|---------|---------|
| logback（默认） | `ctool4j-log-base` + `ctool4j-logback` |
| log4j2 | `ctool4j-log-base` + `ctool4j-log4j` |

> logback 与 log4j2 模块互斥，按需二选一引入（底层实现不同，slf4j 门面统一）。
> 仅需请求日志自动埋点（拦截器/advice），可只引入 `ctool4j-log-base`。

## 快速上手

### 使用 logback

```xml
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-log-base</artifactId>
</dependency>
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-logback</artifactId>
</dependency>
```

引入后自动获得 `spring-boot-starter-logging`（logback 后端），MDC 跨线程透传开箱即用。

### 使用 log4j2

```xml
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-log-base</artifactId>
</dependency>
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-log4j</artifactId>
</dependency>
```

引入后自动获得 `spring-boot-starter-log4j2`（log4j2 后端，含 slf4j 桥接实现），同时自动排除 `spring-boot-starter-logging` 中的 logback 与反向桥接，避免冲突。模块内置 `log4j2-spring.xml` 配置模板。
