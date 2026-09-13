# ctool4j-test-definition

> 共享测试模型：可复用的测试数据载体与测试用类型定义。

## 简介

`ctool4j-test-definition` 提供与测试框架无关的测试模型（实体、枚举、函数式接口样例等），
供各模块的单元测试与 `ctool4j-test-core` / `ctool4j-test-spring` 复用，避免在每个模块重复造测试数据。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-definition` | compile | 基础定义（实体基类、语义接口等） |
| `lombok` | provided | 由 `ctool4j-parent` 下发 |

## 相关入口

- 模块族总览：[ctool4j-test-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
