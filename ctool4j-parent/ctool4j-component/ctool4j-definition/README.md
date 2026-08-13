# ctool4j-definition

> 基础定义层：为全项目提供统一的基础类型约定——返回体、实体基类、业务枚举、函数式接口与语义化接口。

## 简介

`ctool4j-definition` 是全项目依赖链的最底层（不依赖任何其他 ctool4j 模块）。它不包含业务逻辑，只定义「公共契约」，供 `ctool4j-core` 及各上层模块复用，保证整套工具库的类型口径一致。

## 功能特性

- 统一返回模型 `CResult`（code / msg / data 结构，提供 success / error 静态工厂）
- 实体基类与字段接口族：主键、创建/更新时间、创建/更新人
- 语义化接口族：`ICCode`、`ICMsg`、`ICName`、`ICValue`、`ICData`、`ICEvent` 等
- 函数式接口族：`CFunction`、`CConsumer`、`CSupplier`、`CBiConsumer`、`CTriFunction`、`StartEndTimeConsumer` 等（可抛出异常）
- 通用业务枚举：币种、国家码、客户端类型、平台类型、版本、MIME 类型、数据库操作类型
- 业务注解：`@CBizId`（业务 ID）、`@CJsonLog`（JSON 日志标记）、`@CLogBlob`（大字段日志标记）

## 包结构

| 包 | 用途 |
|----|------|
| `annotation` | 业务注解：`@CBizId` / `@CJsonLog` / `@CLogBlob` |
| `constant` | 全局常量（`CTool4jConstants`、`CTool4jTestConstants`） |
| `enums` | 通用枚举（数据库操作、MIME 类型、币种、国家码、客户端/平台、版本） |
| `interfaces` | 语义化接口族（code / msg / name / value / data / event / password 等） |
| `function` | 自定义函数式接口族 |
| `model` | 统一返回模型 `CResult` 及返回结构接口（`ICCodeMessageDataResult` 等） |
| `entity.base` | 实体基类与字段接口（ID、创建/更新时间、创建/更新人） |

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CResult<DATA>` | 模型 | 统一返回体（code / msg / data），`success()` / `success(data)` / `error(msg)` / `error(code, msg)` 静态工厂 |
| `ICCodeMessageDataResult` | 接口 | 返回体结构契约（code + msg + data） |
| `CBaseEntity` | 实体基类 | 基础实体（ID + 创建/更新时间等字段组合） |
| `CId` / `CLongId` / `CStringId` / `CIntegerId` | 实体基类 | 不同主键类型的实体基类 |
| `CCreateTime` / `CUpdateTime` / `CCreateBy` / `CUpdateBy` | 实体基类 | 审计字段实体基类 |
| `ICId` / `ICCreateTime` / `ICUpdateTime` 等 | 接口 | 实体字段接口族（供 MP 填充等场景识别） |
| `CBizId` | 注解 | 标记实体的业务 ID 字段（配合 mybatis 模块的 `CBizIdUtils`） |
| `CLogBlob` | 注解 | 标记需脱敏/截断的大字段（配合 core 的 `CLogBlobSerializer`） |
| `CDbOperateEnum` | 枚举 | 数据库操作类型（增删改查） |
| `CMimeTypeEnum` | 枚举 | MIME 类型 |
| `CCurrencyEnum` | 枚举 | 币种 |
| `CClientTypeEnum` / `CPlatformTypeEnum` | 枚举 | 客户端 / 平台类型 |
| `CFunction<T, R>` 等 | 函数接口 | 可抛异常的 Supplier / Function / Consumer / TriFunction 族 |

## 使用示例

```java
// 统一返回体
return CResult.success(user);            // code=200, msg=OK
return CResult.error("用户不存在");      // 服务端错误返回
return CResult.error(1001, "参数错误");  // 自定义错误码

// 实体继承基类
public class User extends CBaseCreateTimeEntity {
    // 自动拥有 id、createTime 等公共字段
}
```

## 配置项

无（纯类型定义模块，不涉及运行时配置）。

## 依赖

| 依赖 | 说明 |
|------|------|
| `hutool` | 字符串等基础工具 |
| `spring-web`（HttpStatus） | 返回体状态码 |
| `lombok` | 编译期简化 |
