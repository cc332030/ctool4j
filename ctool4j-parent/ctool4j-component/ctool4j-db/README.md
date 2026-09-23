# ctool4j-db

> 数据库 SQL 拼接工具：基于实体属性 Lambda 安全构建 SQL 片段，避免手写列名出错。

## 简介

`ctool4j-db` 提供轻量级的 SQL 片段生成能力：通过方法引用（Lambda）获取实体字段名，自动转换为下划线风格数据库列名，并支持表别名前缀与多条件拼接。

## 功能特性

- 属性 Lambda 转数据库列名（驼峰 → 下划线）
- 生成 select 列 SQL（支持表别名）
- 生成等值 / 大于条件 SQL（支持左右别名）
- 多条件按分隔符（逗号 / AND / OR）拼接
- 分页 limit SQL、行锁 `for update` SQL 常量

## 包结构

| 包 | 用途 |
|----|------|
| `util` | `CSqlUtils` SQL 拼接工具 |
| `enums` | `CSqlSeparatorEnum` 拼接分隔符枚举 |

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CSqlUtils` | 工具类 | SQL 片段构建：`toColumnName` / `getColumnsSql` / `getEqualsSql` / `getGreaterSql` / `limitSql` / `forUpdate` |
| `CSqlSeparatorEnum` | 枚举 | 拼接分隔符：`COMMA`（逗号）、`AND`、`OR` |

## 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` | 字符串工具、分页配置 |
| `hutool` | Lambda 解析、Pair |

## 使用与配置（引用方）

引入坐标、用法示例、配置项、误用点等**面向引用方**的内容：见使用文档 [`doc/use/db.adoc`](../../../doc/use/db.adoc)（整体开放为静态服务），本 README 不再重复（同一事实两个真源必然漂移）。
