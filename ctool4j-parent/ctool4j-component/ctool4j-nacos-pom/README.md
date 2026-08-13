# ctool4j-nacos-pom

> Nacos 聚合模块：`ctool4j-nacos-discovery`（Feign 本地实例注册）、`ctool4j-nacos-config`（占位）。

## 简介

`ctool4j-nacos-pom` 是聚合 pom，包含 2 个子模块。核心能力是 `ctool4j-nacos-discovery`：本地开发时将 Feign 调用的目标实例手动注册进 Nacos，解决本地多服务联调时的服务发现问题。

---

## 子模块一：ctool4j-nacos-discovery

> Feign 本地实例注册：本地联调时把目标服务实例注册进 Nacos 命名服务。

### 功能特性

- 启动时把配置的本地实例（serviceName → ip:port）批量注册进 Nacos（`registerInstance`）
- 关闭时自动反注册（`deregisterInstance`），避免残留脏实例
- 通过 `@ConditionalOnProperty` 开关控制，仅本地环境启用

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CFeignLocalClientConfig` | 配置 | `feign.client.local-instance` 属性：urls 映射（serviceName → ip:port） |
| `CFeignLocalClientInit` | 初始化 | 启动注册 / 关闭反注册本地实例 |

### 使用示例

```yaml
feign:
  client:
    local-instance:
      enabled: true
      urls:
        order-service: 127.0.0.1:8081
        user-service: 127.0.0.1:8082
```

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-definition` / `ctool4j-spring` | 基础能力与生命周期 |
| `nacos-client` / `spring-cloud-nacos` | Nacos 客户端 |

---

## 子模块二：ctool4j-nacos-config

> 占位模块，暂无源码（配置中心封装待实现，配置依赖由业务自行引入）。

## 模块选择建议

- 仅本地联调场景 → `ctool4j-nacos-discovery`
- 需要配置中心 → 等待 `ctool4j-nacos-config` 实现或自行引入 `spring-cloud-starter-alibaba-nacos-config`
