# ctool4j-mybatis-base

> MyBatis-Plus 分层基础：Mapper/Service 分层契约、分页、SQL 注入的通用父级。

## 简介

`ctool4j-mybatis-base` 提供与 MyBatis-Plus **版本无关**的通用能力，供 `ctool4j-mybatis-33` / `ctool4j-mybatis-34` / `ctool4j-mybatis`
三个版本适配模块复用：`CBaseMapper` / `CServiceImpl` 等分层契约、`ICBizService` 业务 ID 方法模式、分页配置与 SQL 注入支撑。
业务项目一般不直接引入本模块，而是按 MP 版本引入对应的适配模块（见 [模块 README](../README.md)）。

## 源目录布局

公共源码目录 `src/main/java`（外加 `src/main/resources`）承版本无关内容；**版本适配层 `spi`**（原
`ctool4j-mybatis-adapter`，已并回）落在两个档位源目录：

| 目录 | 承载 |
|------|------|
| `java` | 版本无关主代码：`ICService` 一族（含 `ICCheckService`）、`CServiceImpl`、`CMpController`，以及 mapper/model/handler/configuration/util |
| `java-jdk8` | `spi` 适配层，承 MP < 3.5.17 的 `extension.service` 包名（jdk8 档） |
| `java-latest` | `spi` 适配层，承 MP >= 3.5.17 的 `spring.service` 包名（最新 LTS 档） |
| `resources` | 资源 |

**版本侧差异收敛在本模块的 `spi` 档位源目录**：mybatis-plus 3.5.17 起 `IService`/`ServiceImpl` 由
`com.baomidou.mybatisplus.extension.service` 迁到 `com.baomidou.mybatisplus.spring.service`，
两个档位目录以同一组中性全限定名（`com.c332030.ctool4j.mybatisplus.spi`）同包同名承接、按档位只编其一
（追加语义天然互斥），公共契约 `ICService` 一族因此与版本无关。

**容器侧差异也不在本模块**：`CMpController` 的参数级必填用 ctool4j-web 的中立注解 `@CRequired`
（`javax.validation` 与 `jakarta.validation` 的统一抽象），公共源目录不按档位拆。

> 除 `spi` 的 `java-jdk8` / `java-latest` 外，本模块**不得**再出现其他源目录，
> 也不得 `srcDir` 指向别的模块——多源只允许出现在 `spi` 适配层。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-core` | compile（api） | 工具与空安全实现（`CList` 等） |
| `ctool4j-transaction` | compile | `@CTransactional` 事务约定 |
| `ctool4j-db` | compile | SQL 拼接工具 |
| `ctool4j-web` | provided | `@CRequired` 等中立校验注解（`CMpController` 用） |
| `ctool4j-redis` | provided | 分布式锁；由使用方引入 Redis 依赖后生效 |
| `ctool4j-definition` / `ctool4j-spring` | 经 `ctool4j-transaction` 传递 | 定义契约与 Spring 基础设施 |
| `mybatis-plus-boot-starter` | provided | MP 抽象；**不锁定版本**，由使用方按其 MP 版本提供 |
| `spring-boot-starter-jdbc` | provided | 由 `ctool4j-mybatis-pom` 统一下发 |

> 使用方需自行引入 `mybatis-plus-boot-starter` 与 `spring-boot-starter-jdbc`（两者在本模块为 provided，不传递）。

## 相关入口

- 模块族总览：[ctool4j-mybatis-pom README](../README.md)
- 版本适配设计：[doc/design/mybatis/version-adapter.adoc](../../../../doc/design/mybatis/version-adapter.adoc)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
