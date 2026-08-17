# ctool4j-core

> 核心工具库：字符串、集合、日期、数值、JSON、反射、Bean、异常、日志、校验等 Java 通用能力的集中封装。

## 简介

`ctool4j-core` 是整套工具库的能力基座，提供与业务无关的通用工具类（以 `C` 前缀命名）。它不依赖 Spring 容器，可直接在任意 Java 项目中使用。

## 功能特性

- 字符串工具 `CStrUtils`（格式化、截断、脱敏、转换）
- 集合工具 `CCollUtils`、`CMapUtils`、`CList`、`CSet`、`CCollectors`、`CStreamUtils`
- 日期工具 `CDateUtils`（格式化、区间、偏移）
- 数值工具 `CNumUtils`、`CAmountUtils`（金额精度）、`CBase62Utils`、`CBase64Utils`
- 类与反射：`CBeanUtils`（属性拷贝）、`CClassUtils`、`CReflectUtils`、`CObjUtils`（泛型解析）、`CLambdaUtils`、`CMethodHandleUtils`、`CConvertUtils`
- JSON 工具 `CJsonUtils` + Jackson 定制（日期/枚举序列化、`@CLogBlob` 大字段日志序列化）
- 异常体系：`CBusinessException`（业务异常）、`CExceptionUtils`、异常提供者 SPI
- 日志工具：`CLog`（JSON 日志门面）、`CLogUtils`
- 校验工具：`CAssert`（断言）、`CValidateUtils`
- 其他：`CIdUtils`、`CUrlUtils`、`CDesUtils`、`CEnumUtils`、`CComparatorUtils`、`COpt`、`CResultUtils`、`CSpiUtils`、`CPageUtils`、`CThreadLocalUtils`、本地缓存工具等

## 包结构

| 包 | 用途 |
|----|------|
| `classes` | 类相关工具：Bean 拷贝、Class 操作、反射、Lambda、泛型解析 |
| `util` | 通用工具：字符串 / 集合 / 日期 / 数值 / JSON / URL / 枚举 / 可选值 |
| `jackson` | Jackson 定制：日期与枚举序列化、`CLogBlobSerializer`、反序列化器 |
| `log` | 日志：`CLog` JSON 日志、`CLogUtils` |
| `exception` | 异常体系：业务异常、异常工具、异常提供者 SPI |
| `validation` | 校验：断言、校验工具 |
| `cache` | Class 值缓存工具（含强引用/弱引用实现） |
| `config` / `enums` / `interfaces` / `mapstruct` | 配置、枚举、语义接口、MapStruct 配置 |

## 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CStrUtils` | 工具类 | 字符串格式化、去空格、截断、脱敏、驼峰/下划线转换 |
| `CCollUtils` | 工具类 | 集合判空、去重、分组、转换、分页切片 |
| `CMapUtils` | 工具类 | Map 构建、取值、转换 |
| `CDateUtils` | 工具类 | 日期解析/格式化、区间计算、偏移、本周本月工具 |
| `CNumUtils` | 工具类 | 数值解析、比较、默认值 |
| `CJsonUtils` | 工具类 | JSON 序列化 / 反序列化（封装 Jackson） |
| `CBeanUtils` | 工具类 | 对象属性拷贝、Map↔Bean 转换 |
| `CReflectUtils` | 工具类 | 反射操作：方法调用、字段读写、注解扫描 |
| `CObjUtils` | 工具类 | 对象判空、类型解析（含 lambda 方法引用的泛型解析） |
| `CClassUtils` | 工具类 | Class 加载、实例化 |
| `CConvertUtils` | 工具类 | 类型转换（含枚举、日期） |
| `CEnumUtils` | 工具类 | 枚举按 code/name 查找 |
| `CLog` | 门面类 | 日志门面，输出 JSON 结构化日志 |
| `CLogUtils` | 工具类 | 日志格式化工具 |
| `CBusinessException` | 异常 | 业务异常基类 |
| `CExceptionUtils` | 工具类 | 异常封装：受检异常转非受检、日志输出 |
| `CAssert` | 工具类 | 断言校验，失败抛业务异常 |
| `CValidateUtils` | 工具类 | 通用校验 |
| `CLogBlobSerializer` | 序列化器 | `@CLogBlob` 大字段的日志序列化（脱敏/截断） |
| `CJacksonUtils` | 工具类 | Jackson ObjectMapper 封装与配置 |

## 使用示例

```java
// 字符串
String name = CStrUtils.ifBlank(value, "默认值");
String masked = CStrUtils.maskPhone("13800000000");

// 集合
List<String> list = CCollUtils.toList("a", "b", "c");

// JSON
User user = CJsonUtils.toObj(jsonStr, User.class);
String json = CJsonUtils.toJson(user);

// 异常
throw new CBusinessException("用户不存在");
CAssert.notNull(obj, "对象不能为空");

// 日志
CLog.info("用户操作", CLogUtils.of("userId", 1L, "action", "login"));
```

## 配置项

| 配置 | 说明 |
|------|------|
| `c-page.*` | 分页默认配置（`CPageConfig`），供各模块分页工具读取 |

## 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-definition` | 基础类型（返回体、函数式接口等） |
| `hutool` | 底层工具库 |
| `jackson` | JSON 序列化 |
| `lombok` | 编译期简化 |
