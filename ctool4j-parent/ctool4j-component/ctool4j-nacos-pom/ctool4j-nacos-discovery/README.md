# ctool4j-nacos-discovery

> Feign 本地实例注册：本地联调时把目标服务实例注册进 Nacos 命名服务。

## 简介

本地开发时多个业务服务常需跨服务联调，而本地实例不在注册中心里。本模块在应用启动时把配置的本地实例
（`serviceName → ip:port`）批量注册进 Nacos（`registerInstance`），关闭时自动反注册（`deregisterInstance`），
避免残留脏实例影响其它环境的调用。

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CFeignLocalClientConfig` | 配置 | `feign.client.local-instance` 属性：`urls` 映射（serviceName → ip:port） |
| `CFeignLocalClientInit` | 初始化 | 启动注册 / 关闭反注册本地实例 |

## 使用方式

```yaml
feign:
  client:
    local-instance:
      enabled: true
      urls:
        order-service: 127.0.0.1:8081
        user-service: 127.0.0.1:8082
```

`enabled=true` 才生效（`@ConditionalOnProperty`），仅建议在本地环境开启。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-spring-cloud` | compile | Spring Cloud 基础封装与生命周期回调 |
| `spring-cloud-starter-alibaba-nacos-discovery` | compile | Nacos 注册发现客户端 |

## 相关入口

- 模块族总览：[ctool4j-nacos-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
