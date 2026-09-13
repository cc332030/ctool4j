# ctool4j-mq-base

> **占位模块**：消息队列基础能力，待实现。

## 简介

`ctool4j-mq-base` 规划承载与具体 MQ 产品无关的公共能力（消息模型、发送/消费抽象、traceId 透传等），
供 `ctool4j-rabbitmq`、`ctool4j-rocketmq` 复用。**当前无源码**。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-spring` | compile | Spring 基础设施（为后续实现预留） |

## 相关入口

- 模块族总览：[ctool4j-mq-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
