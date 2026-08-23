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

## 使用示例

```java
// 属性 -> 列名（驼峰转下划线）
CSqlUtils.toColumnName(User::getUserName);   // user_name

// 字段列表 + 表别名
CSqlUtils.getColumnsSql(List.of(User::getId, User::getUserName), "t");
// t.id,t.user_name

// 等值条件（t1 与 t2 表关联）
CSqlUtils.getEqualsSql(User::getId, "t1", Dept::getUserId, "t2");
// t1.id = t2.user_id

// 多等值条件，OR 拼接
CSqlUtils.getEqualsSql(pairs, CSqlSeparatorEnum.OR);

// 分页 / 行锁
CSqlUtils.limitSql(10);   // limit 10
CSqlUtils.forUpdate();    // for update
```

## 配置项

无。

## 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` | 字符串工具、分页配置 |
| `hutool` | Lambda 解析、Pair |
