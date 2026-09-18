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

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-definition` / `ctool4j-spring` | 基础能力与生命周期 |
| `nacos-client` / `spring-cloud-nacos` | Nacos 客户端 |

---

## 子模块二：ctool4j-nacos-config

> 占位模块，暂无源码（配置中心封装待实现，配置依赖由业务自行引入）。

## 使用与配置（引用方）

引入坐标、用法示例、配置项、模块选型、误用点等**面向引用方**的内容：见使用文档 [`doc/use/nacos.adoc`](../../../doc/use/nacos.adoc)（整体开放为静态服务），本 README 不再重复（同一事实两个真源必然漂移）。
