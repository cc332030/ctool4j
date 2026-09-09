# ctool4j-redis

> Redis 操作与分布式锁：通用读写服务、带 TTL 的值模型、Redisson 分布式锁封装。

## 简介

`ctool4j-redis` 封装 Spring Data Redis 与 Redisson，提供 String / 对象两种取值服务、分布式锁服务与静态工具，供缓存、安全等上层模块复用。

## 功能特性

- **Redis 服务**：`ICRedisService` 统一抽象（hasKey / setIfAbsent / opsForValue 等），`CStringStringRedisService`（String 键值）、`CObjectValueRedisService`（对象序列化存取）
- **带 TTL 值**：`CValueWithTtl` 封装「值 + 过期时间」
- **分布式锁**：`CLockService` 提供 `lock` / `tryLock` / 链式 `tryLockThenRun`；`CLockUtils` 静态门面
- **自动装配**：`CRedisConfiguration` / `CRedisInit` 启动初始化
- **限流**：`@CRateLimit` 注解 + `CRateLimitAspect` 切面，基于 Redis 固定窗口计数，支持按业务 id 维度限流
- **幂等**：`@CIdempotent` 注解 + `CIdempotentAspect` 切面，基于 Redis 分布式锁防重复提交与并发穿透

## 包结构

| 包 | 用途 |
|----|------|
| `configuration` | Redis 装配与初始化 |
| `model` | 带 TTL 值模型 `CValueWithTtl` |
| `service` / `service.impl` | Redis 服务接口与实现 |
| `rate` | 限流注解 / 切面 / 异常 |
| `idempotent` | 幂等注解 / 切面 / 异常 |
| `util` | 锁 / Redis 静态工具 |

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `ICRedisService` | 接口 | Redis 通用操作抽象 |
| `CStringStringRedisService` | 实现 | String 键值读写 |
| `CObjectValueRedisService` | 实现 | 对象序列化读写（含 TTL） |
| `CValueWithTtl` | 模型 | 值 + 过期时间封装 |
| `CLockService` | 服务 | 分布式锁（lock / tryLock / tryLockThenRun 链式） |
| `CLockUtils` | 工具类 | 分布式锁静态门面 |
| `CRedisUtils` | 工具类 | Redis 操作静态门面 |
| `CRedisKeyUtils` | 工具类 | 业务 key 构建与业务 id 解析公共工具 |
| `CRateLimit` | 注解 | 方法级限流注解 |
| `CRateLimitAspect` | 切面 | 限流切面（计数 + 阈值判断） |
| `CRateLimitException` | 异常 | 限流触发异常 |
| `CIdempotent` | 注解 | 方法级幂等注解 |
| `CIdempotentAspect` | 切面 | 幂等切面（分布式锁防并发） |
| `CIdempotentException` | 异常 | 幂等冲突异常 |
| `CRedisConfiguration` | 配置 | Redis 自动装配 |

## 使用示例

```java
// 对象读写
redisService.setValue("user:" + id, user, Duration.ofMinutes(30));
User user = redisService.getValue("user:" + id, User.class);

// 分布式锁
lockService.tryLockThenRun("order:" + orderId, Duration.ofSeconds(10), () -> {
    // 临界区
});

// 静态工具
CLockUtils.tryLock("key", () -> { ... });

// 限流：60 秒内每个用户最多 3 次
@CRateLimit(id = "userId", count = 3, interval = 60)
public Order createOrder(Long userId, OrderReq req) {
    return orderService.create(req);
}

// 幂等：同一 userId 同一时刻仅允许一次下单（防重复提交/并发穿透）
@CIdempotent(group = Order.class, id = "userId")
public Order submitOrder(Long userId, OrderReq req) {
    return orderService.submit(req);
}
```

## 配置项

| 配置前缀 | 说明 |
|----------|------|
| `spring.data.redis.*` | Spring Data Redis 标准配置 |

## 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-spring` | Spring 基础设施 |
| `spring-boot-starter-data-redis` | Redis 客户端 |
| `redisson-spring-boot-starter` | Redisson 分布式锁 |
