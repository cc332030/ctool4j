# ctool4j-doc-pom

> 接口文档聚合模块：通用接口文档描述注解（`ctool4j-doc-base`）、OpenAPI2（knife4j / springfox）增强（`ctool4j-doc-openapi2`）。

## 简介

`ctool4j-doc-pom` 是聚合 pom，包含 2 个子模块：

- `ctool4j-doc-base`：通用接口文档描述注解（CSchema/CTag/COperation/CParameter），供各文档实现（如 openapi2）复用
- `ctool4j-doc-openapi2`：Swagger / springfox 接口文档增强（Docket 自动装配、全局请求头注入、校验注解自动映射必填参数、兼容性修复）

## 子模块一：ctool4j-doc-base

> 通用接口文档描述注解模块（纯声明，不依赖任何文档框架）。

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CSchema` | 注解 | 字段/getter 文档描述（对齐 ApiModelProperty/@Schema） |
| `CTag` | 注解 | 接口分组标签（替代 @Api） |
| `COperation` | 注解 | 操作摘要/说明（替代 @ApiOperation） |
| `CParameter` | 注解 | 参数说明/名称/示例（纯文档，替代 @ApiParam） |

设计文档见 `doc/design/doc-base/`。

## 子模块二：ctool4j-doc-openapi2

> Swagger / springfox 增强模块。

### 功能特性

- **Docket 自动装配**：`COpenApi2Configuration` 自动装配 Docket、注册插件
- **全局请求头**：`CSpringFoxUtils` 生成全局 header 参数（基于 `CRequestHeaderEnum`）
- **必填参数映射**：`ICExpandedParameterBuilderPlugin` 参数扩展插件，将 `@CRequired`/`@NotEmpty` 注解参数在文档中标记为必填
- **兼容性修复**：修复 springfox 空指针问题

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-doc-base` | 通用文档描述注解 |
| `ctool4j-core` | 工具与请求头接口 |
| `ctool4j-web` | 请求头枚举、校验注解（CRequired/CNotRequired） |
| `springfox` / `knife4j` / `swagger` | 文档框架 |
