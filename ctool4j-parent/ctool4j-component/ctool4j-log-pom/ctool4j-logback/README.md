# ctool4j-logback

> logback 日志后端适配（默认后端）。

## 简介

基于 `ctool4j-log-base` 的日志能力，显式声明 logback 后端：把 `spring-boot-starter-logging` 由父级的 `provided`
覆盖为 `compile`，从而**传递到使用方**，保证日志实现随本模块一并落地。

## 使用方式

```xml
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-logback</artifactId>
</dependency>
```

与 `ctool4j-log4j` 二选一，不得同时引入（会出现多个 SLF4J 实现绑定）。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-log-base` | compile | 请求日志、traceId 透传、MDC、日志级别热更新 |
| `spring-boot-starter-logging` | compile | logback 后端；覆盖父级 provided，传递到使用方 |

## 相关入口

- 模块族总览：[ctool4j-log-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
