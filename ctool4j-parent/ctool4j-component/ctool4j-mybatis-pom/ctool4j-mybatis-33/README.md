# ctool4j-mybatis-33

> MyBatis-Plus 3.3 版本适配模块。

## 简介

在 `ctool4j-mybatis-base` 的分层契约之上，注册 MyBatis-Plus **3.3** 版本对应的分页插件（`PaginationInterceptor`，
`@ConditionalOnMissingBean` 可被业务覆盖），并按 3.3 的方法签名注入 `insertIgnore` / `updateAllById`（`CSqlInjector`、
`CInsertIgnoreMethod` / `CUpdateAllByIdMethod` 手工构造 SQL）。

## 使用方式

```xml
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-mybatis-33</artifactId>
</dependency>
```

业务项目按所用 MyBatis-Plus 版本在 `ctool4j-mybatis-33` / `ctool4j-mybatis-34` / `ctool4j-mybatis` 中三选一，不叠加引入。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-mybatis-base` | compile | 分层契约与 SQL 注入基础 |
| `mybatis-plus-boot-starter` | compile | MP 3.3.x（版本由使用方 / BOM 决定） |

## 相关入口

- 模块族总览：[ctool4j-mybatis-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
