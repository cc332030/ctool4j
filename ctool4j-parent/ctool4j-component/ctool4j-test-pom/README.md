# ctool4j-test-pom

> 测试支撑聚合模块：共享测试模型（`ctool4j-test-definition`），核心与 Spring 测试模块占位。

## 简介

`ctool4j-test-pom` 是聚合 pom，包含 3 个子模块。当前仅 `ctool4j-test-definition` 有源码，提供测试用例共享的实体 / DTO / 响应模型。

## 子模块状态

| 子模块 | 状态 | 内容 |
|--------|------|------|
| `ctool4j-test-definition` | 有源码 | 测试模型：`User` 实体、`UserDto`（请求 DTO）、`UserRsp`（响应模型） |
| `ctool4j-test-core` | 空模块 | 测试核心能力（待实现） |
| `ctool4j-test-spring` | 空模块 | Spring 测试集成（待实现） |

## 子模块一：ctool4j-test-definition

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `User` | 实体 | 基础测试用户实体（userName / password / age） |
| `UserDto` | 模型 | 请求 DTO，继承 User 扩展请求字段 |
| `UserRsp` | 模型 | 响应模型，继承 User（含 BigDecimal 等字段） |

### 依赖

| 依赖 | 说明 |
|------|------|
| `lombok` | 编译期简化 |

## 使用说明

各测试用例可复用 `ctool4j-test-definition` 中的模型作为测试数据载体；`ctool4j-test-core`、`ctool4j-test-spring` 能力待后续实现。
