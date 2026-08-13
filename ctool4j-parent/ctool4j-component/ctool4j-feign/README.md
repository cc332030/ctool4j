# ctool4j-feign

> Feign 客户端增强：请求头跨服务传播、请求/响应完整日志、自定义拦截器扩展。

## 简介

`ctool4j-feign` 基于 OpenFeign 提供微服务调用增强能力，解决微服务间请求头（token、traceId 等）透传与调用日志记录问题。

## 功能特性

- **请求头传播**：`CFeignInterceptor` 自动把当前请求的关键请求头传递给 Feign 调用，支持 `CFeignClientHeaderPropagationModeEnum` 配置传播模式
- **完整日志**：`CFeignLogger` 记录请求/响应体；`CFeignClient` 包装默认 Client 捕获响应日志
- **自定义拦截器**：`@CCustomerFeignInterceptor` 标记业务自定义 Feign 拦截器
- **请求头配置**：`CFeignClientHeaderConfig` 支持按服务配置要传递的请求头
- **工具类**：`CFeignUtils`（HTTP 日志上下文等）

## 包结构

| 包 | 用途 |
|----|------|
| `annotation` | `@CCustomerFeignInterceptor` 自定义拦截器标记 |
| `client` | Feign Client 封装（响应日志捕获） |
| `config` | 请求头 / 日志 / 全局配置属性 |
| `configuration` | Feign 装配与初始化 |
| `enums` | 请求头传播模式枚举 |
| `interceptor` | 请求头传递拦截器 |
| `log` | Feign 日志实现 |
| `util` | Feign 工具 |

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CFeignInterceptor` | 拦截器 | 请求头跨服务传递 |
| `ICRequestInterceptor` | 接口 | 请求头提供抽象 |
| `CFeignLogger` | 日志 | 请求/响应体完整日志 |
| `CFeignClient` | 客户端 | 包装默认 Client，捕获响应 |
| `CFeignConfiguration` | 配置 | Feign 全局装配 |
| `CFeignClientHeaderConfig` | 配置 | 按服务配置传播请求头 |
| `CFeignClientHeaderPropagationModeEnum` | 枚举 | 传播模式（如全部/白名单/关闭） |
| `CCustomerFeignInterceptor` | 注解 | 标记自定义拦截器 |
| `CFeignUtils` | 工具类 | Feign 通用工具 |

## 使用示例

```java
// 服务间调用：自动透传当前请求的 token / traceId 等请求头
@FeignClient(name = "order-service", configuration = CFeignConfiguration.class)
public interface OrderClient {
    @GetMapping("/order/{id}")
    Order getById(@PathVariable Long id);
}
```

## 配置项

| 配置前缀 | 说明 |
|----------|------|
| `feign.client.header.*` | 请求头传播配置（模式、白名单） |
| `feign.client.log.*` | Feign 日志配置 |

## 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-spring-cloud` | Spring Cloud 能力 |
| `ctool4j-log-base` | 请求日志与 traceId 透传 |
| `openfeign` | Feign 客户端 |
| `feign-httpclient`（provided） | HTTP 客户端增强 |
