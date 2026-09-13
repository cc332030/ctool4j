# ctool4j-log4j

> log4j2 日志后端适配。

## 简介

基于 `ctool4j-log-base` 的日志能力，把日志后端切换为 log4j2：引入 `spring-boot-starter-log4j2`，并**排除**默认的
`spring-boot-starter-logging`（logback），使使用方以 log4j2 作为 SLF4J 实现。

## 使用方式

引入本模块即完成后端切换，无需额外配置：

```xml
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-log4j</artifactId>
</dependency>
```

与 `ctool4j-logback` 二选一，不得同时引入（会出现多个 SLF4J 实现绑定）。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-log-base` | compile | 请求日志、traceId 透传、MDC、日志级别热更新 |
| `spring-boot-starter-logging` | compile | 声明后**排除**，用于剔除默认 logback |
| `spring-boot-starter-log4j2` | compile | log4j2 后端 |

## 相关入口

- 模块族总览：[ctool4j-log-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
