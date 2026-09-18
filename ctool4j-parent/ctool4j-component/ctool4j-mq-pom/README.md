# ctool4j-mq-pom

> 消息队列聚合模块（占位，待实现）。

## 简介

`ctool4j-mq-pom` 是聚合 pom，规划 3 个子模块：`ctool4j-mq-base`（消息队列通用抽象）、`ctool4j-rabbitmq`（RabbitMQ 集成）、`ctool4j-rocketmq`（RocketMQ 集成）。当前均为空模块（仅 pom，无源码），为后续消息队列能力预留模块结构。

## 子模块状态

| 子模块 | 状态 | 规划能力 |
|--------|------|----------|
| `ctool4j-mq-base` | 空模块 | 消息体封装、发送/监听抽象 |
| `ctool4j-rabbitmq` | 空模块 | RabbitMQ 生产者/消费者封装 |
| `ctool4j-rocketmq` | 空模块 | RocketMQ 生产者/消费者封装 |

## 依赖

无（占位模块）。

## 使用与配置（引用方）

引入坐标、用法示例、配置项、模块选型、误用点等**面向引用方**的内容：见使用文档 [`doc/use/mq.adoc`](../../../doc/use/mq.adoc)（整体开放为静态服务），本 README 不再重复（同一事实两个真源必然漂移）。
