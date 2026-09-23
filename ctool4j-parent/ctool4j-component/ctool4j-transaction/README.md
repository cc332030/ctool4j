# ctool4j-transaction

> 事务注解约定：`@CTransactional` 提供更安全的事务默认值（默认回滚所有异常）。

## 简介

`ctool4j-transaction` 是一个极小的模块，仅提供一个事务注解 `@CTransactional`，它是 Spring `@Transactional` 的组合别名注解，默认回滚策略更贴近业务习惯。

## 功能特性

- 默认 `rollbackFor = Exception.class`（所有异常回滚，避免受检异常不生效的坑）
- 完整透传 Spring 事务属性：传播行为、隔离级别、只读
- 支持类级与方法级使用（`@Inherited`）

## 包结构

| 包 | 用途 |
|----|------|
| `annotation` | `CTransactional` 事务注解 |

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CTransactional` | 注解 | `@Transactional` 的别名注解，`@AliasFor` 透传 propagation / isolation / readOnly / rollbackFor |

## 依赖

| 依赖 | 说明 |
|------|------|
| `spring-tx` | 事务抽象 |

## 使用与配置（引用方）

引入坐标、用法示例、配置项、误用点等**面向引用方**的内容：见使用文档 [`doc/use/transaction.adoc`](../../../doc/use/transaction.adoc)（整体开放为静态服务），本 README 不再重复（同一事实两个真源必然漂移）。
