# ctool4j-rabbitmq

> **占位模块**：RabbitMQ 适配，待实现。

## 简介

`ctool4j-rabbitmq` 规划提供 RabbitMQ 的自动配置与使用约定（含 `TODO.adoc` 中的默认 channel 数据限制、自定义名称等），
依赖 `ctool4j-mq-base` 的公共抽象。**当前无源码**。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-mq-base` | compile | 消息队列公共抽象 |
| `spring-boot-starter-amqp` | compile | RabbitMQ 客户端 |

## 相关入口

- 模块族总览：[ctool4j-mq-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
