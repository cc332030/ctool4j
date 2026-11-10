# ctool4j-mybatis-adapter

> MyBatis-Plus 版本适配层：把 `IService` / `ServiceImpl` 的包名迁移差异收敛在本模块，使用方感知不到版本。

## 简介

mybatis-plus 3.5.17 起把 `IService` / `ServiceImpl` 从
`com.baomidou.mybatisplus.extension.service` 迁到 `com.baomidou.mybatisplus.spring.service`，
两个坐标不能共存于同一 classpath。

本模块用**同一组中性全限定名**（包 `com.c332030.ctool4j.mybatisplus.spi`）承接，
内部再按 JDK 档位去承对应版本的 MP 坐标。于是：

- 上层（`ctool4j-mybatis-base` 的 `ICService` 一族）源码对四个 MP 版本一字不改；
- 消费者拿到的是**按档位编好的 jar**，不需要再从别的模块 `srcDir` 挂载源码。

## 源目录布局（全仓唯一允许出现多源的模块）

| 目录 | 内容 | 生效档位 |
|------|------|----------|
| `src/main/java` | 承 `com.baomidou.mybatisplus.spring.service`，对应 MP 3.5.17+ | 最新 LTS（默认） |
| `src/main/java-jdk8` | 承 `com.baomidou.mybatisplus.extension.service`，对应 MP 3.3.x / 3.4.x / 3.5.16 | jdk8 |

本模块**只有这两个源目录**：它的全部内容就是三个"按版本侧各写一份"的类，版本无关的东西不放这里
（`ICService` 一族与 `CServiceImpl` 都在 `ctool4j-mybatis-base`）。「当前/默认」侧用标准目录
`src/main/java`，「旧档」侧挂 `java-jdk8`——与 Maven 的"主源码目录 + profile 专属目录"习惯一致。

两个目录承载**完全相同的相对路径集合**（互为镜像），差异只在"承哪个 MP 包名"上。它们**永不共存**：
同包同名的两份 class 同时进 classpath 时，JVM 取先命中者且**不报错**。根脚本对 `java-<side>` 是
**追加**语义，本模块两目录同名同包、追加即重复，故 jdk8 档位由本模块的 `build.gradle.kts` 把主源集
**整体替换**为 `java-jdk8`（全仓唯一这样做的模块）。

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `spi/IService` | 接口 | 对外沿用的 service 名（中性全限定名），成员全部继承自 `CMybatisPlusSpi` |
| `spi/CMybatisPlusSpi` | 接口 | 自适应层：承接 MP `IService`，并提供空安全元素级方法与计数收口 |
| `spi/CMybatisPlusServiceImpl` | 抽象类 | 继承桥：承对应版本的 `ServiceImpl`，零逻辑 |

`spi/CMybatisPlusSpi` 的两处"版本吸收"：

- **空安全**：`getById(null)` → `null`、`getOptById(null)` → `Optional.empty()`、
  `listByIds(空集合)` → 空列表，全部短路、不触发查询；
- **计数**：`cCount(LambdaQueryChainWrapper)` 吸收 `ChainQuery#count()` 的返回值漂移
  （3.3.x 返回 `Integer`、3.4.x 起返回 `Long`，**描述符不兼容**）。jdk8 档产物要同时服务
  3.3.x / 3.4.x / 3.5.16，故那一侧用方法句柄取运行期返回值、再按 `Number` 收口；
  最新 LTS 档只对 3.5.17+ 成立，直接调用。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `mybatis-plus-boot-starter` | provided（compileOnly） | MP 抽象；**不向下游传递**，避免与 `-33` / `-34` 的 `strictly` 钉版冲突 |
| `ctool4j-core` | compile（api） | `CList`、`CMethodHandleUtils` |

## 相关入口

- 模块族总览：[ctool4j-mybatis-pom README](../README.md)
- 设计说明：[doc/design/mybatis/version-adapter.adoc](../../../../doc/design/mybatis/version-adapter.adoc)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
