# ctool4j-parent

> 组件模块的父聚合：`ctool4j-pom`（构建父级）与 `ctool4j-component`（组件模块集合）。

## 简介

`ctool4j-parent` 是 ctool4j 组件层的入口聚合模块，向上 import `ctool4j-bom` 统一版本，向下把公共依赖下发给全部组件模块，
是「构建基础设施 → 组件模块」两层结构的中间层。

## 模块结构

| 子模块 | 定位 | 说明 |
|--------|------|------|
| [ctool4j-pom](ctool4j-pom/README.md) | 构建父级聚合 | `ctool4j-boot-parent`（Spring Boot 应用父级）、`ctool4j-processor-parent`（注解处理器使用方父级） |
| [ctool4j-component](ctool4j-component/README.md) | 组件模块集合 | definition / core / web / redis / cache / mybatis / auth / log / mq / job / file / doc / test 等组件 |

## 公共约定

本模块（作为全部组件模块的父级）统一提供以下依赖，组件模块**无需重复声明**：

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-autowired-processor` | provided | 编译期注入代码生成 |
| `lombok` | provided | 编译期简化 |
| `spring-boot-starter-test` | test | 单元测试支撑 |

同时通过 `dependencyManagement` import `ctool4j-bom`，使组件模块声明依赖无需写版本号。

模块间依赖关系见 [doc/dependency.adoc](../doc/dependency.adoc)；完整模块索引见 [根 README](../README.adoc)。
