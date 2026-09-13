# ctool4j-pom

> 构建父级聚合：`ctool4j-boot-parent`（Spring Boot 应用父级）、`ctool4j-processor-parent`（使用注解处理器的模块父级）。

## 简介

`ctool4j-pom` 是构建视角的聚合模块（packaging=pom），不含业务代码，只提供两类使用方父级：
面向 Spring Boot 应用的 `ctool4j-boot-parent`，以及面向使用编译期注解处理器模块的 `ctool4j-processor-parent`。

## 子模块

| 子模块 | 定位 | 依赖 | 详细文档 |
|--------|------|------|----------|
| `ctool4j-boot-parent` | Spring Boot 应用父级：绑定 `spring-boot-maven-plugin`、把部署插件统一为 `deployAtEnd` | 无模块依赖 | [pom.xml](ctool4j-boot-parent/pom.xml) |
| `ctool4j-processor-parent` | 使用注解处理器模块的父级：统一下发 `ctool4j-mybatis-processor`、`ctool4j-mq-processor`（provided）与编译参数 | 两个处理器（provided） | [pom.xml](ctool4j-processor-parent/pom.xml) |

## 使用方式

业务应用 `parent` 指向 `ctool4j-boot-parent` 即可获得 Spring Boot 打包与发布约定；
使用代码生成的业务模块 `parent` 指向 `ctool4j-processor-parent`，无需自行声明处理器。

模块间依赖关系见 [doc/dependency.adoc](../../doc/dependency.adoc)。
