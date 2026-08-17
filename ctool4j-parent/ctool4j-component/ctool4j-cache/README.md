# ctool4j-cache

> 二级缓存框架：本地缓存（Caffeine/Guava）+ Redis 分布式缓存，配合分布式锁实现防击穿、防雪崩的智能刷新。

## 简介

`ctool4j-cache` 提供声明式（`@CCacheable` 注解）与编程式（`CCacheService` 链式 builder）两种缓存使用方式，根据缓存剩余有效期自动分流：充足则直返，临期则异步刷新，过期则加锁双检查计算，兼顾性能与一致性。

## 功能特性

- **声明式缓存**：`@CCacheable` 注解 + `CCacheAspect` 切面，支持本地/Redis 双级、命名空间、过期时间、自定义缓存 ID 转换器
- **缓存 ID**：`@CCacheId` 标记实体作为缓存 key 的字段；`ICCacheIdConverter` / `CDefaultCacheIdConverter` 自定义 key 生成策略
- **编程式缓存**：`CCacheService.cacheBuilder(key, tClass)` 返回链式构建器：
  - `expireDuration`：固定或按值动态计算过期时间
  - `refreshWindow`：临期刷新窗口（窗口内异步刷新不阻塞主线程，带节流防并发）
  - `waitTime` / `onLockFail`：分布式锁等待与抢锁失败回调
  - `computeIfAbsent(supplier)`：核心取值入口
- **静态门面**：`CCacheUtils` 代理 `cacheBuilder`
- 提供兼容旧接口的 `setValue` / `getCache` 方法

## 包结构

| 包 | 用途 |
|----|------|
| `annotation` | `@CCacheable` / `@CCacheId` 缓存注解 |
| `aop` | `CCacheAspect` 切面与缓存 ID 转换器 |
| `service` | `CCacheService`（含 `CCacheBuilder`）核心服务 |
| `util` | `CCacheUtils` 静态门面 |

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CCacheable` | 注解 | 缓存标记：local/redis、namespace、expire、idConverter |
| `CCacheId` | 注解 | 标记实体字段作为缓存 key |
| `CCacheAspect` | 切面 | 拦截 `@CCacheable`，分流本地与 Redis 缓存 |
| `ICCacheIdConverter` | 接口 | 缓存 ID 生成策略 |
| `CDefaultCacheIdConverter` | 实现 | 默认缓存 ID 生成（基于 `@CCacheId` 字段） |
| `CCacheService` | 服务 | 核心缓存服务，提供链式 `cacheBuilder` |
| `CCacheService.CCacheBuilder` | 构建器 | 链式缓存取值：expireDuration / refreshWindow / waitTime / computeIfAbsent |
| `CCacheUtils` | 工具类 | 静态门面，代理 `cacheBuilder` |

## 使用示例

```java
// 声明式缓存
@CCacheable(namespace = "user", expire = 60)
public User getById(Long id) {
    return userMapper.selectById(id);
}

// 编程式缓存（链式）
User user = cacheService.cacheBuilder("user:" + id, User.class)
    .expireDuration(Duration.ofMinutes(30))       // 固定过期
    .refreshWindow(Duration.ofMinutes(5))         // 临期异步刷新
    .waitTime(Duration.ofSeconds(10))             // 锁等待时长
    .onLockFail(lock -> log.warn("抢锁失败，key={}", lock.getName()))
    .computeIfAbsent(() -> userMapper.selectById(id));
```

## 配置项

| 配置前缀 | 说明 |
|----------|------|
| `spring.redis.*` / `spring.data.redis.*` | Redis 连接配置 |
| `jetcache.*` | jetcache 本地缓存配置（Caffeine/Guava） |

## 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-redis` | Redis 操作与分布式锁 |
| `jetcache-starter-redis-lettuce` | 本地 + Redis 二级缓存实现 |
| `spring-boot-starter-data-redis`（provided） | Redis 客户端 |
