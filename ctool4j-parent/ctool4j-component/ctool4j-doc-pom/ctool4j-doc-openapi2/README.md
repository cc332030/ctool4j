# ctool4j-doc-openapi2

> OpenAPI2（knife4j / springfox）接口文档增强。

## 简介

`ctool4j-doc-openapi2` 在 `ctool4j-web` 之上提供接口文档增强：以 `knife4j-openapi2-spring-boot-starter`
（springfox + knife4j）为底座，复用 `ctool4j-definition` 的接口文档描述注解（`doc.annotation` 包）与
`ctool4j-web` 的请求头枚举、校验注解，形成统一的接口描述与调试入口。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-web` | compile | Web 通用能力、请求头枚举 `CRequestHeaderEnum`、校验注解 |
| `knife4j-openapi2-spring-boot-starter` | compile | OpenAPI2 文档框架 |
| `ctool4j-definition` / `ctool4j-core` | 经 `ctool4j-web` 传递 | 文档描述注解、工具类 |

## 相关入口

- 模块族总览：[ctool4j-doc-pom README](../README.md)
- 功能级设计文档：[doc/design/definition/openapi-doc-annotations.adoc](../../../../doc/design/definition/openapi-doc-annotations.adoc)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
