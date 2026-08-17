# ctool4j-redis

> Redis 操作与分布式锁：通用读写服务、带 TTL 的值模型、Redisson 分布式锁封装。

## 简介

`ctool4j-redis` 封装 Spring Data Redis 与 Redisson，提供 String / 对象两种取值服务、分布式锁服务与静态工具，供缓存、安全等上层模块复用。

## 功能特性

- **Redis 服务**：`ICRedisService` 统一抽象（hasKey / setIfAbsent / opsForValue 等），`CStringStringRedisService`（String 键值）、`CObjectValueRedisService`（对象序列化存取）
- **带 TTL 值**：`CValueWithTtl` 封装「值 + 过期时间」
- **分布式锁**：`CLockService` 提供 `lock` / `tryLock` / 链式 `tryLockThenRun`；`CLockUtils` 静态门面
- **自动装配**：`CRedisConfiguration` / `CRedisInit` 启动初始化

## 包结构

| 包 | 用途 |
|----|------|
| `configuration` | Redis 装配与初始化 |
| `model` | 带 TTL 值模型 `CValueWithTtl` |
| `service` / `service.impl` | Redis 服务接口与实现 |
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
