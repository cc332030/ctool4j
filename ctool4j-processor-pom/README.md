# ctool4j-processor-pom

> 编译期注解处理器聚合模块：`ctool4j-processor-base`、`ctool4j-autowired-processor`、`ctool4j-mybatis-processor`、`ctool4j-mq-processor`。

## 简介

编译期代码生成能力的聚合 pom。处理器通过 SPI 自动注册，被组件模块以 annotation processor 方式使用；
本聚合模块自身不含依赖，也不参与运行时。

## 子模块

| 子模块 | 定位 | 依赖 | 详细文档 |
|--------|------|------|----------|
| `ctool4j-processor-base` | 处理器基础（SPI 骨架与共用支撑） | 无 | [pom.xml](ctool4j-processor-base/pom.xml) |
| `ctool4j-autowired-processor` | 处理 `@CAutowired` / `@CAutowiredScan`，为静态字段生成构造器注入类 | `ctool4j-processor-base` | [pom.xml](ctool4j-autowired-processor/pom.xml) |
| `ctool4j-mybatis-processor` | 处理 `@CAutoBizService`，按实体 getter 生成业务 Service 接口 | `ctool4j-processor-base`、`ctool4j-mybatis`（test） | [README](ctool4j-mybatis-processor/README.md) |
| `ctool4j-mq-processor` | 占位，待实现 | `ctool4j-processor-base` | [pom.xml](ctool4j-mq-processor/pom.xml) |

## 使用方式

组件模块通常无需手工声明：`ctool4j-parent` 已把 `ctool4j-autowired-processor` 作为 provided 依赖下发给全部组件模块；
业务项目按需引入对应处理器（scope=provided）。

模块间依赖关系见 [doc/dependency.adoc](../doc/dependency.adoc)。
