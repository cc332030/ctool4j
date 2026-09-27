# ctool4j-mybatis-base

> MyBatis-Plus 分层基础：Mapper/Service 分层契约、分页、SQL 注入的通用父级。

## 简介

`ctool4j-mybatis-base` 提供与 MyBatis-Plus **版本无关**的通用能力，供 `ctool4j-mybatis-33` / `ctool4j-mybatis-34` / `ctool4j-mybatis`
三个版本适配模块复用：`CBaseMapper` / `CServiceImpl` 等分层契约、`ICBizService` 业务 ID 方法模式、分页配置与 SQL 注入支撑。
业务项目一般不直接引入本模块，而是按 MP 版本引入对应的适配模块（见 [模块 README](../README.md)）。

## 源目录布局

本模块的 `src/main/` 下除 `resources` 外共六个目录，**它们之间代码完全互斥**（同一全限定名任一时刻只出现一次）：
`java` 由本模块自己编译；其余五个是"挂在 base 名下、由各版本模块按需 `srcDir` 纳入"的源码，base 自身不编译。

| 目录 | 承载 | 挂载者 |
|------|------|--------|
| `java` | base 自编（MP 版本无关） | 本模块 |
| `java-mp` | 版本无关公共面：`ICService` 一族、`CBizIdUtils`、`CMybatisPlusSide` | 三个版本模块 |
| `java-mp-bridge` | **3.5.x 侧**适配桥（`spi/IService`、`spi/IMybatisPlusSpi`、`spi/CMybatisPlusServiceImpl`、`service/ICCheckService`、`service/impl/CBaseServiceImpl`） | `ctool4j-mybatis` |
| `java-mp-bridge-ext` | **3.3.x/3.4.x 侧**适配桥（相对路径集合与 `java-mp-bridge` 完全镜像，仅承的 MP 坐标不同） | `ctool4j-mybatis-33` / `-34` |
| `java-mp-jdk8-ext` | jdk8 档位专属 `CMpController`（`javax.validation`） | 各模块（jdk8 档） |
| `java-mp-latest-ext` | 最新 LTS 档位专属 `CMpController`（`jakarta.validation`） | 各模块（最新 LTS 档） |

**互斥的三条判定**（改动后可用 `git ls-files` 逐条复核）：

1. `java-mp` 的源码不引用任何版本侧符号（不出现对 `java-mp-bridge*` 独有类型的引用）——公共桶与版本侧解耦；
2. `java-mp-bridge` 与 `java-mp-bridge-ext` 的**相对路径集合完全一致**（互为镜像），但只有一侧被任一模块挂载，故不会出现重复全限定名；
3. 任一模块挂载的目录集合里，全限定名去重后数量等于文件数（无重名）。

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
