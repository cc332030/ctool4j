# ctool4j-test-spring

> 测试支撑（部分占位）：Spring 场景的测试辅助。

## 简介

`ctool4j-test-spring` 规划承载 Spring 上下文相关的测试辅助（组合测试注解、容器辅助等），
在 `ctool4j-test-core` 之上叠加 Spring 能力。**当前能力部分占位**；Spring 场景的组合测试注解见
`ctool4j-spring` 的 `@CTool4jSpringBootTest`。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-spring` | compile | Spring 基础设施 |
| `ctool4j-test-core` | compile | 核心测试辅助 |
| `spring-boot-starter-test` | provided | 测试框架；由使用方提供 |

## 相关入口

- 模块族总览：[ctool4j-test-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
