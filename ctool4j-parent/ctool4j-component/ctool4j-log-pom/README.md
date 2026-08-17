# ctool4j-log-pom

> 日志聚合模块：请求日志自动埋点（log-base）、logback 适配（logback）、log4j 适配（占位）。

## 简介

`ctool4j-log-pom` 是聚合 pom，包含 3 个子模块：`ctool4j-log-base`（请求日志自动埋点与日志级别接口族）、`ctool4j-logback`（MDC 跨线程透传与日志级别热更新）、`ctool4j-log4j`（占位）。

---

## 子模块一：ctool4j-log-base

> HTTP 请求日志自动埋点（拦截器 + advice）。

### 功能特性

- **自动埋点**：`CRequestLogHandlerInterceptor`（拦截器）+ `CLogRequestBodyAdvice` / `CLogResponseBodyAdvice`（ControllerAdvice）自动收集请求/响应日志
- **日志级别接口族**：`ICLogLevel` 及 Debug/Info/Error/Warn/Trace 子接口（按场景输出不同级别）

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CRequestLogHandlerInterceptor` | 拦截器 | 请求前后埋点、慢请求告警、清理 ThreadLocal |
| `CLogRequestBodyAdvice` | advice | 读取请求体写入请求日志 |
| `CLogResponseBodyAdvice` | advice | 响应写出前写请求日志 |
| `ICLogLevel` 族 | 接口 | 日志级别接口族 |

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

- **MDC 跨线程透传**：`CMdc` 实现 slf4j `MDCAdapter`，用 TTL ThreadLocal 承载 MDC，配合线程池使用实现 traceId 跨线程传递
- **日志级别热更新**：`CLogLevelListener` 监听 `EnvironmentChangeEvent`，动态热更新 logback 日志级别（读 `logging.level.*`）

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CMdc` | 适配器 | TTL ThreadLocal 版 MDCAdapter |
| `CLogLevelListener` | 监听器 | 运行时日志级别热更新 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` | 核心工具 |
| `ctool4j-spring` | 事件监听、代理 |
| `logback` | 日志后端 |

---

## 子模块三：ctool4j-log4j

> 占位模块，暂无源码（log4j 后端适配待实现）。

## 模块选择建议

- 需要请求日志自动埋点（拦截器/advice）→ 引入 `ctool4j-log-base`
- 使用 logback → 引入 `ctool4j-log-base` + `ctool4j-logback`
- 使用其他日志后端 → 引入 `ctool4j-log-base`（与后端无关）
