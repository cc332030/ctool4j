# ctool4j-mybatis-base

> MyBatis-Plus 分层基础：Mapper/Service 分层契约、分页、SQL 注入的通用父级。

## 简介

`ctool4j-mybatis-base` 提供与 MyBatis-Plus **版本无关**的通用能力，供 `ctool4j-mybatis-33` / `ctool4j-mybatis-34` / `ctool4j-mybatis`
三个版本适配模块复用：`CBaseMapper` / `CServiceImpl` 等分层契约、`ICBizService` 业务 ID 方法模式、分页配置与 SQL 注入支撑。
业务项目一般不直接引入本模块，而是按 MP 版本引入对应的适配模块（见 [模块 README](../README.md)）。

## 源目录布局

本模块的 `src/main/` 下除 `resources` 外共三个目录，**它们之间代码完全互斥**（同一全限定名任一时刻只出现一次）：

| 目录 | 承载 | 编译者 |
|------|------|--------|
| `java` | 版本无关公共面：`ICService` 一族、`CBizIdUtils`，以及 mapper/model/handler/configuration/util | 本模块 |
| `java-javax` | jdk8 档位容器侧 `CMpController`（`javax.validation`） | 本模块（jdk8 档） |
| `java-jakarta` | 最新 LTS 档位容器侧 `CMpController`（`jakarta.validation`） | 本模块（最新 LTS 档） |

`java-javax` ↔ `java-jakarta` 是**成对镜像**（同包同名、各承一侧容器类型、按 JDK 档位二选一），
由根构建脚本的档位源码目录机制整体放行——与 `ctool4j-http-servlet` 等同构。
两目录互为镜像（除 `javax`/`jakarta` 包名外逐字节一致），一致性由「成对文件互引 + 归一化比对」维护。

**版本侧差异不在本模块**：mybatis-plus 3.5.17 起 `IService`/`ServiceImpl` 由
`com.baomidou.mybatisplus.extension.service` 迁到 `com.baomidou.mybatisplus.spring.service`，
该差异已下沉到中间模块 [`ctool4j-mybatis-mp-java8`](../ctool4j-mybatis-mp-java8/README.md)，
由其中的自适应层（`spi/CMybatisPlusSpi` 两侧各一份）吸收，公共契约 `ICService` 一族因此与版本无关。

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
