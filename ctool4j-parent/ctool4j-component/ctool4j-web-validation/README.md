# ctool4j-web-validation

Bean Validation 侧的**必填校验注解**模块：`@CRequired` / `@CNotRequired` 与 `CRequiredValidator`。

独立成模块的原因只有一个：`CRequired` 依赖 Bean Validation API，而该 API 的包名在 jdk8 档位是
`javax.validation`、最新 LTS 档位是 `jakarta.validation`——同一个全限定名必须有两份实现。
把它留在 `ctool4j-web` 内，会让那个模块的 46 个无关类一起被迫按档位拆主源目录；
放进容器适配层（`ctool4j-http-servlet`）又会破坏那一层"只做容器类型转换"的契约。

规范依据见 `agent/AGENTS-PROJECT.MD` 的「javax/jakarta 双栈（抽象层 + 容器适配）」一节。

## 内容

| 类型 | 全限定名 | 说明 |
| --- | --- | --- |
| 注解 | `com.c332030.ctool4j.web.validation.annotation.CRequired` | 标注即必填（无 `required` 开关）：字符串→`notBlank`、集合/Map/数组→`notEmpty`、其他→`notNull`，按值类型自动分发；支持自定义 `message` |
| 注解 | `com.c332030.ctool4j.web.validation.annotation.CNotRequired` | 纯标记：request param 在文档上标记非必填（不参与 SpringMVC 绑定） |
| 校验器 | `com.c332030.ctool4j.web.validation.validator.CRequiredValidator` | `@CRequired` 的实现，复用 `ctool4j-core` 的 `CValidUtils` |

## 依赖与档位

- 只声明 Bean Validation **API**（jdk8 → `javax.validation:validation-api`，其余 → `jakarta.validation:jakarta.validation-api`，由版本目录的 `validation-api` / `validation-api-jdk8` 按档位取）；**实现**（hibernate-validator）由使用方的 `spring-boot-starter-validation` 提供。
- `@CRequired` 的**全限定名在两档位完全一致**：`ctool4j-doc-openapi2` 按注解类型读取、`ctool4j-mybatis-base` 的 `CMpController` 双档位编译，都依赖这一点，坐标变动不影响它们。

两套源码同包同名、互为镜像：`src/main/java`（jakarta）与 `src/main/java-javax`（javax）下的同名文件
除 `javax`/`jakarta` 字样外应**完全一致**。`CNotRequired` 是纯标记注解、与 Bean Validation 无关，
两侧逐字节一致——跟着成对放置，是为了让本模块只有一种源码形态（不必为它多出一层公共目录）。

```bash
# 两档位各构建一次，即可确认两侧源码都被编译到
gradle -DJDK_VERSION=8  :ctool4j-web-validation:build
gradle -DJDK_VERSION=25 :ctool4j-web-validation:build
```

## 相关入口

- 使用方：[ctool4j-web](../ctool4j-web/README.md)（`api` 依赖本模块，业务侧经 `ctool4j-web` 即可用）
- 组件层索引：[ctool4j-component](../README.md)
- 模块间依赖关系：[doc/dependency.adoc](../../../doc/dependency.adoc)
