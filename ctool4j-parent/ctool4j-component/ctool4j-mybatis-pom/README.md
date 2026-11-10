# ctool4j-mybatis-pom

> MyBatis-Plus 增强聚合模块：业务分层契约（Mapper / Service / Controller）+ 分页 + SQL 注入 + 锁执行，按 MP 版本提供适配。

## 简介

`ctool4j-mybatis-pom` 是聚合 pom，包含 4 个子模块：

| 模块 | 角色 |
|------|------|
| `ctool4j-mybatis-base` | 核心契约与增强基座（`ICService` 一族、`CServiceImpl`、`CMpController`、分页/注入/业务 ID 工具）；**内含版本适配层 `spi`**（原 `ctool4j-mybatis-adapter`，已并回）：MP 包名差异（`extension.service` ↔ `spring.service`）到此为止，档位源目录 `src/main/java-jdk8` 与 `src/main/java-latest` 同包同名、按档位二选一 |
| `ctool4j-mybatis-33` | MP 3.3 适配（旧版分页插件、旧版注入器签名），**仅 jdk8 档位构建** |
| `ctool4j-mybatis-34` | MP 3.4 适配（新版插件体系、新版注入器签名），**仅 jdk8 档位构建** |
| `ctool4j-mybatis` | 当前主推聚合版（MP 3.5.x：jdk8 档 3.5.16、最新 LTS 档 3.5.17） |

设计说明见 [`doc/design/mybatis/version-adapter.adoc`](../../../doc/design/mybatis/version-adapter.adoc)。

---

## 子模块一：ctool4j-mybatis-base

> MyBatis-Plus 面向业务的分层增强基座（纯接口/抽象，不注册 Bean）；其 `spi` 档位源目录把 `IService` / `ServiceImpl` 的包名迁移差异收敛掉，使用方感知不到版本。

mybatis-plus 3.5.17 起把 `IService` / `ServiceImpl` 从 `com.baomidou.mybatisplus.extension.service`
迁到 `com.baomidou.mybatisplus.spring.service`，两个坐标不能共存。**版本适配层 `spi`**（原 `ctool4j-mybatis-adapter`，已并回）
用**同一组中性全限定名**（`com.c332030.ctool4j.mybatisplus.spi`）承接，靠两个档位专属源目录二选一：

| 目录 | 内容 | 生效档位 |
|------|------|----------|
| `src/main/java-latest` | 承 `spring.service` 包名，对应 MP 3.5.17+ | 最新 LTS |
| `src/main/java-jdk8` | 承 `extension.service` 包名，对应 MP 3.3.x / 3.4.x / 3.5.16 | jdk8 |

档位目录里**只有三个按版本侧各写一份的类**，版本无关的代码都在公共源目录 `src/main/java`。

两个目录**永不共存**：同一次编译按档位只追加其一（追加语义天然互斥），不存在同包同名重复。

核心类：

| 类 | 类型 | 职责 |
|----|------|------|
| `spi/IService` | 接口 | 对外沿用的 service 名（中性全限定名） |
| `spi/CMybatisPlusSpi` | 接口 | 自适应层：空安全元素级方法 + `cCount(...)` 计数收口 |
| `spi/CMybatisPlusServiceImpl` | 抽象类 | 继承桥：承对应版本的 `ServiceImpl`，零逻辑 |

`cCount(...)` 吸收 `ChainQuery#count()` 的返回值漂移（3.3.x `Integer` / 3.4.x 起 `Long`，
描述符不兼容）：jdk8 档产物要同时服务三个版本，故那一侧用方法句柄取运行期返回值、再按
`Number` 收口；最新 LTS 档只对 3.5.17+ 成立，直接调用。

### 功能特性

- **Mapper 扩展**：`CBaseMapper` 追加 `insertIgnore`、`updateAllById` 自定义方法（配合 SQL 注入器实现）
- **Service 分层**：
  - `ICService`：核心 service 契约（实体工厂、多字段按值增删改查、分页 page 系列、saveIgnore / updateAllById）
  - `ICBizIdService` / `ICBizService` / `ICMainBizService`：按业务 ID / 主业务 ID 维度增删改查与统计
  - `ICMpLockService`：基于 Redis 锁的插入/更新/删除加锁执行
  - `ICCheckService`：空值防御（根契约；空安全实现由适配模块 `spi/CMybatisPlusSpi` 提供）
  - `CServiceImpl`：service 实现基类（继承适配模块的实现桥、实现 `ICService`、消歧元素级方法、解析实体类型）
- **Controller 基类**：`CMpController` 提供 `/page`、`/get-by-id`、`/add`、`/update-by-id`、`/remove-by-id` 标准 CRUD 端点
- **SQL 注入契约**：`ICMpMethod` / `ICMpSqlMethod` / `CMpSqlMethod`（INSERT_IGNORE、UPDATE_ALL_BY_ID）
- **业务 ID 工具**：`CBizIdUtils` 扫描 `@CBizId` 注解字段，生成 / 读取 / 回填业务 ID
- **分页工具**：`CMpPageUtils` 按场景建分页对象、`pageThenDo` / `pageThenEach` 批量处理、MP 分页对象转分页结果
- **分页模型**：请求侧 `ICPage` / `CPage` / `CPageReq`，响应侧 `ICPageResult` / `CPageResult`；逻辑删除契约 `ICDeleted` / `CDeleted`（`@TableLogic`）

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CBaseMapper` | Mapper | 扩展 BaseMapper，追加 insertIgnore / updateAllById |
| `ICService` | 接口 | 核心 Service 契约 |
| `ICBizService` | 接口 | 按业务 ID 增删改查/统计 |
| `ICMainBizService` | 接口 | 按主业务 ID 查询统计 |
| `ICMpLockService` | 接口 | Redis 锁加锁 CRUD |
| `CServiceImpl` | 实现 | service 实现基类（承 `spi/CMybatisPlusServiceImpl` + `ICService`，消歧元素级方法，泛型解析实体类型） |
| `CMpController` | 控制器 | 标准 CRUD 端点基类 |
| `CBizIdUtils` | 工具类 | 业务 ID 生成与回填 |
| `CMpPageUtils` | 工具类 | 分页与批量处理 |
| `CMpSqlMethod` | 枚举 | 自定义 SQL 方法定义 |
| `CPage` / `CPageReq` | 模型 | 分页请求模型 |
| `CPageResult` | 模型 | 分页结果模型（不依赖 MP，作 `IPage` 的响应契约替代） |

### 源目录

公共源码目录 `src/main/java`（外加 `src/main/resources`）承版本无关内容；版本适配层 `spi` 落档位源目录（见上）：

- 版本差异由本模块的 `spi` 档位源目录吸收；
- 容器（`javax` / `jakarta`）差异由 ctool4j-web 的中立类型吸收（`CMpController` 的参数级必填用
  `@CRequired`，公共源目录不按档位拆）。

除 `spi` 的 `java-jdk8` / `java-latest` 外，本模块**不得**再出现其他源目录，也不得
`srcDir` 指向别的模块。

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` / `ctool4j-definition` | 核心工具 |
| `ctool4j-spring` | Spring 能力 |
| `ctool4j-web`（provided） | `@CRequired` 等中立校验注解 |
| `ctool4j-redis`（provided） | 分布式锁 |
| `mybatis-plus`（provided，泛型引用，不锁定版本） | MP 抽象；不向下游传递 MP 版本，避免与 `-33` / `-34` 的 `strictly` 钉版冲突 |

---

## 子模块二：ctool4j-mybatis-33（MP 3.3 适配）

- 注册旧版 `PaginationInterceptor` 分页插件（`@ConditionalOnMissingBean`）
- `CSqlInjector` 按 3.3 签名注入 insertIgnore / updateAllById
- `CInsertIgnoreMethod` / `CUpdateAllByIdMethod` 手工构造 SQL
- 版本钉住 `3.3.2`（`strictly`）；**只在 jdk8 档位参与构建**（3.3.x 只有 `extension.service` 包名，
  与最新 LTS 档的 base jar 不同侧）

---

## 子模块三：ctool4j-mybatis-34（MP 3.4 适配）

- 采用新版插件体系：`MybatisPlusInterceptor` + `PaginationInnerInterceptor` + `BlockAttackInnerInterceptor`（防全表更新/删除）
- `CSqlInjector` 按 3.4 签名（getMethodList 带 Configuration / TableInfo）
- `CInsertIgnoreMethod` / `CUpdateAllByIdMethod` 同 base 契约
- 版本钉住 `3.4.3.4`（`strictly`）；**只在 jdk8 档位参与构建**

---

## 子模块四：ctool4j-mybatis（当前聚合版）

- 综合 base + 版本实现，采用 MP 新版插件体系
- `CInsertIgnoreMethod` 继承 MP `Insert` 类 + 字符串替换实现（简洁、自动处理自增列）
- `CSqlInjector` 新版签名，读取 dbConfig 配置
- MP 版本按档位取：jdk8 → 3.5.16、最新 LTS → 3.5.17

## 版本差异速览

| 维度 | mybatis-33 | mybatis-34 | mybatis（聚合版） |
|------|-----------|-----------|-----------------|
| MP 版本 | 3.3.2 | 3.4.3.4 | 3.5.16 / 3.5.17 |
| 构建档位 | jdk8 | jdk8 | 两档 |
| 分页插件 | PaginationInterceptor（旧） | MybatisPlusInterceptor + Pagination/BlockAttack（新） | 同 34（新） |
| CInsertIgnoreMethod | 手拼 SQL | 手拼 SQL（单例写法差异） | 继承 MP Insert + 字符串替换 |
| CServiceImpl | 共用 base 的 `CServiceImpl` | 同左 | 同左 |
| CSqlInjector 签名 | 3.3 单参 | 3.4 三参 | 3.4 三参 |

## 使用与配置（引用方）

引入坐标、用法示例、配置项、模块选型、误用点等**面向引用方**的内容：见使用文档 [`doc/use/mybatis.adoc`](../../../doc/use/mybatis.adoc)（整体开放为静态服务），本 README 不再重复（同一事实两个真源必然漂移）。
