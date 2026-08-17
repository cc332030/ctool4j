# ctool4j-mybatis-pom

> MyBatis-Plus 增强聚合模块：业务分层契约（Mapper / Service / Controller）+ 分页 + SQL 注入 + 锁执行，按 MP 版本提供适配。

## 简介

`ctool4j-mybatis-pom` 是聚合 pom，包含 4 个子模块：`ctool4j-mybatis-base`（核心契约与增强基座）、`ctool4j-mybatis-33`（MP 3.3 适配）、`ctool4j-mybatis-34`（MP 3.4 适配）、`ctool4j-mybatis`（当前聚合版）。以「接口契约 + 版本实现」方式避免绑定具体 MyBatis-Plus 版本。

---

## 子模块一：ctool4j-mybatis-base

> MyBatis-Plus 面向业务的分层增强基座（纯接口/抽象，不注册 Bean）。

### 功能特性

- **Mapper 扩展**：`CBaseMapper` 追加 `insertIgnore`、`updateAllById` 自定义方法（配合 SQL 注入器实现）
- **Service 分层**：
  - `ICService`：核心 service 契约（实体工厂、多字段按值增删改查、分页 page 系列、saveIgnore / updateAllById）
  - `ICBizIdService` / `ICBizService` / `ICMainBizService`：按业务 ID / 主业务 ID 维度增删改查与统计
  - `ICMpLockService`：基于 Redis 锁的插入/更新/删除加锁执行
  - `ICCheckService`：空值防御
- **Controller 基类**：`CMpController` 提供 `/page`、`/get-by-id`、`/add`、`/update-by-id`、`/remove-by-id` 标准 CRUD 端点
- **SQL 注入契约**：`ICMpMethod` / `ICMpSqlMethod` / `CMpSqlMethod`（INSERT_IGNORE、UPDATE_ALL_BY_ID）
- **业务 ID 工具**：`CBizIdUtils` 扫描 `@CBizId` 注解字段，生成 / 读取 / 回填业务 ID
- **分页工具**：`CMpPageUtils` 按场景建分页对象、`pageThenDo` / `pageThenEach` 批量处理
- **分页模型**：`ICPage` / `CPage` / `CPageReq`；逻辑删除契约 `ICDeleted` / `CDeleted`（`@TableLogic`）

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CBaseMapper` | Mapper | 扩展 BaseMapper，追加 insertIgnore / updateAllById |
| `ICService` | 接口 | 核心 Service 契约 |
| `ICBizService` | 接口 | 按业务 ID 增删改查/统计 |
| `ICMainBizService` | 接口 | 按主业务 ID 查询统计 |
| `ICMpLockService` | 接口 | Redis 锁加锁 CRUD |
| `CBaseServiceImpl` | 实现 | ServiceImpl 桥接实现 |
| `CMpController` | 控制器 | 标准 CRUD 端点基类 |
| `CBizIdUtils` | 工具类 | 业务 ID 生成与回填 |
| `CMpPageUtils` | 工具类 | 分页与批量处理 |
| `CMpSqlMethod` | 枚举 | 自定义 SQL 方法定义 |
| `CPage` / `CPageReq` | 模型 | 分页模型 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` / `ctool4j-definition` | 核心工具 |
| `ctool4j-spring` | Spring 能力 |
| `ctool4j-redis` | 分布式锁 |
| `mybatis-plus`（泛型引用，不锁定版本） | MP 抽象 |

---

## 子模块二：ctool4j-mybatis-33（MP 3.3 适配）

- 注册旧版 `PaginationInterceptor` 分页插件（`@ConditionalOnMissingBean`）
- `CSqlInjector` 按 3.3 签名注入 insertIgnore / updateAllById
- `CInsertIgnoreMethod` / `CUpdateAllByIdMethod` 手工构造 SQL
- `CServiceImpl` 重写 `getEntityClass`（基于 `CObjUtils` 泛型解析）

---

## 子模块三：ctool4j-mybatis-34（MP 3.4 适配）

- 采用新版插件体系：`MybatisPlusInterceptor` + `PaginationInnerInterceptor` + `BlockAttackInnerInterceptor`（防全表更新/删除）
- `CSqlInjector` 按 3.4 签名（getMethodList 带 Configuration / TableInfo）
- `CInsertIgnoreMethod` / `CUpdateAllByIdMethod` 同 base 契约

---

## 子模块四：ctool4j-mybatis（当前聚合版）

- 综合 base + 版本实现，采用 MP 新版插件体系
- `CInsertIgnoreMethod` 继承 MP `Insert` 类 + 字符串替换实现（简洁、自动处理自增列）
- `CSqlInjector` 新版签名，读取 dbConfig 配置
- 当前主推的 MyBatis-Plus 整合模块

## 版本差异速览

| 维度 | mybatis-33 | mybatis-34 | mybatis（聚合版） |
|------|-----------|-----------|-----------------|
| 分页插件 | PaginationInterceptor（旧） | MybatisPlusInterceptor + Pagination/BlockAttack（新） | 同 34（新） |
| CInsertIgnoreMethod | 手拼 SQL | 手拼 SQL（单例写法差异） | 继承 MP Insert + 字符串替换 |
| CServiceImpl | 重写 getEntityClass | 不重写 | 不重写 |
| CSqlInjector 签名 | 3.3 单参 | 3.4 三参 | 3.4 三参 |

## 使用示例

```java
// Service 继承分层契约
@Service
public class UserService extends CBaseServiceImpl<CBaseMapper<User>, User>
    implements ICService<User>, ICBizService<User> {
}

// Mapper
public interface UserMapper extends CBaseMapper<User> {
}

// Controller
@RestController
@RequestMapping("/user")
public class UserController extends CMpController<ICService<User>, User> {
}

// 业务 ID 维度操作
userService.getByBizId(bizId);
userService.listByBizId(bizIds);
userService.countByBizId(bizId);
```

## 配置项

| 配置 | 说明 |
|------|------|
| `mybatis-plus.*` | MyBatis-Plus 标准配置 |

## 模块选择建议

- 项目使用 MyBatis-Plus 3.3 → `ctool4j-mybatis-33`
- 项目使用 MyBatis-Plus 3.4+ → `ctool4j-mybatis-34` 或聚合版 `ctool4j-mybatis`
