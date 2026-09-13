# ctool4j-mybatis-34

> MyBatis-Plus 3.4 版本适配模块（含连表查询可选支持）。

## 简介

在 `ctool4j-mybatis-base` 的分层契约之上完成 MyBatis-Plus **3.4** 的版本适配，并可选接入
`mybatis-plus-join`（MyBatis-Plus-Join，`optional`，使用方引入后生效）以支持连表查询。

## 使用方式

```xml
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-mybatis-34</artifactId>
</dependency>
```

业务项目按所用 MyBatis-Plus 版本在 `ctool4j-mybatis-33` / `ctool4j-mybatis-34` / `ctool4j-mybatis` 中三选一，不叠加引入。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-mybatis-base` | compile | 分层契约与 SQL 注入基础 |
| `mybatis-plus-boot-starter` | compile | MP 3.4.x（版本由使用方 / BOM 决定） |
| `mybatis-plus-join-boot-starter` | optional | 连表查询；使用方按需引入 |

## 相关入口

- 模块族总览：[ctool4j-mybatis-pom README](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
