# ctool4j-mybatis-base

> MyBatis-Plus 分层基础：Mapper/Service 分层契约、分页、SQL 注入的通用父级。

## 简介

`ctool4j-mybatis-base` 提供与 MyBatis-Plus **版本无关**的通用能力，供 `ctool4j-mybatis-33` / `ctool4j-mybatis-34` / `ctool4j-mybatis`
三个版本适配模块复用：`CBaseMapper` / `CServiceImpl` 等分层契约、`ICBizService` 业务 ID 方法模式、分页配置与 SQL 注入支撑。
业务项目一般不直接引入本模块，而是按 MP 版本引入对应的适配模块（见 [模块 README](../README.md)）。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-transaction` | compile | `@CTransactional` 事务约定 |
| `ctool4j-db` | compile | SQL 拼接工具 |
| `ctool4j-redis` | provided | 分布式锁；由使用方引入 Redis 依赖后生效 |
| `ctool4j-core` / `ctool4j-definition` / `ctool4j-spring` | 经 `ctool4j-transaction` 传递 | 工具与 Spring 基础设施 |
| `mybatis-plus-boot-starter` | provided | MP 抽象；**不锁定版本**，由使用方按其 MP 版本提供 |
| `spring-boot-starter-jdbc` | provided | 由 `ctool4j-mybatis-pom` 统一下发 |

> 使用方需自行引入 `mybatis-plus-boot-starter` 与 `spring-boot-starter-jdbc`（两者在本模块为 provided，不传递）。

## 相关入口

- 模块族总览：[ctool4j-mybatis-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
