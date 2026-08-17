# ctool4j-doc-pom

> 接口文档聚合模块：OpenAPI2（knife4j / springfox）增强（`ctool4j-doc-openapi2`）。

## 简介

`ctool4j-doc-pom` 是聚合 pom，包含 1 个子模块 `ctool4j-doc-openapi2`，增强 Swagger / springfox 接口文档能力：Docket 自动装配、全局请求头注入、校验注解自动映射必填参数，并修复 springfox 与新版 Spring 的兼容问题。

## 功能特性

- **Docket 自动装配**：`COpenApi2Configuration` 自动装配 Docket、注册插件
- **全局请求头**：`CSpringFoxUtils` 生成全局 header 参数（基于 `CRequestHeaderEnum`）
- **必填参数映射**：`ICExpandedParameterBuilderPlugin` 参数扩展插件，`CNotEmptyAnnotationPlugin` 将 `@NotEmpty` 注解参数在文档中标记为必填
- **兼容性修复**：修复 springfox 空指针问题

## 包结构

| 包 | 用途 |
|----|------|
| `config` | 文档配置属性 |
| `configuration` | Docket 装配与插件注册 |
| `util` | Docket 构建工具 |
| `plugins.parameter` / `plugins.parameter.impl` | 参数扩展插件接口与实现 |

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `COpenApi2Configuration` | 配置 | 装配 Docket、注册插件、兼容性修复 |
| `CSpringFoxUtils` | 工具类 | 构建 Docket、生成全局 header 参数 |
| `CDocOpenApi2Config` | 配置 | pathMapping 配置（前缀 `c-doc.openapi2`） |
| `ICExpandedParameterBuilderPlugin` | 接口 | springfox 参数扩展插件基础接口 |
| `ICAnnotationExpandedParameterBuilderPlugin` | 接口 | 按注解自动置 required 的插件抽象 |
| `CNotEmptyAnnotationPlugin` | 插件 | `@NotEmpty` 参数标记必填 |

## 配置项

| 配置前缀 | 说明 |
|----------|------|
| `c-doc.openapi2.*` | 文档 pathMapping 等配置 |
| `springfox.*` / `knife4j.*` | springfox / knife4j 标准配置 |

## 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` | 工具与请求头接口 |
| `ctool4j-web` | 请求头枚举 |
| `springfox` / `knife4j` / `swagger` | 文档框架 |
