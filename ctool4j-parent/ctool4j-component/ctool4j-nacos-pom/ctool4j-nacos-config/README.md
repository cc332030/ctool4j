# ctool4j-nacos-config

> **占位模块**：Nacos 配置中心封装，待实现。

## 简介

`ctool4j-nacos-config` 规划承载配置中心的封装能力。**当前无源码**；配置依赖已在 pom 中声明，
业务在使用方侧也可自行引入 `spring-cloud-starter-alibaba-nacos-config` 并直接使用 Spring Cloud 原生配置。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-spring-cloud` | compile | Spring Cloud 基础封装 |
| `spring-cloud-starter-alibaba-nacos-config` | compile | Nacos 配置中心客户端 |

## 相关入口

- 模块族总览：[ctool4j-nacos-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
