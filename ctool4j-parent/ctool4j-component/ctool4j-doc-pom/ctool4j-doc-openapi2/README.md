# ctool4j-doc-openapi2

> OpenAPI2（knife4j / springfox）接口文档增强。

## 简介

`ctool4j-doc-openapi2` 在 `ctool4j-web` 之上提供接口文档增强：以 `knife4j-openapi2-spring-boot-starter`
（springfox + knife4j）为底座，复用 `ctool4j-definition` 的接口文档描述注解（`doc.annotation` 包）与
`ctool4j-web` 的请求头枚举、校验注解，形成统一的接口描述与调试入口。

自动配置入口为 `COpenApi2Configuration`（由 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册）。

## 能力目录

| 能力 | 实现 | 说明 |
|------|------|------|
| 文档入口 | `COpenApi2Configuration#cDocket` | 收集标注 `@Api` 或 `@CTag` 的接口（二者任一命中即纳入）；全局参数含 `AUTHORIZATION` 请求头；业务自建 `Docket` 时不覆盖 |
| 分组 tag | `CTagAnnotationPlugin` | 类级 `@CTag.value` → operation tag，并合并方法级 `@COperation.tags`；`@Order(SWAGGER_PLUGIN_ORDER + 1)` 覆盖 springfox 默认 tags（顺序有测试固化） |
| 操作描述 | `COperationAnnotationPlugin` | `@COperation` 的 value（summary）/description（notes）/deprecated |
| 参数描述 | `CParameterAnnotationPlugin` | `@CParameter` 的 name/description/example；`@CNotRequired` 独立生效置非必填，`@CParameter` 未标 `@CNotRequired` 时默认必填 |
| 参数必填 | `CNotEmptyAnnotationPlugin` / `CRequiredAnnotationPlugin` | `@NotEmpty` / `@CRequired` 命中即必填（默认实现复用 `ICAnnotationExpandedParameterBuilderPlugin`） |
| model 描述与必填 | `CSchemaAnnotationModelPropertyPlugin` | `@CSchema.value` 写属性描述、`@CRequired`（含 `@Repeatable` 容器 `@CRequired.List`）标必填 |
| 枚举 text | `CTextEnumModelPropertyPlugin` / `CTextEnumParameterPlugin` | 实现 `ICText` 的枚举：允许值保持可提交的「枚举名」，`枚举名(text)` 写入 description；已自带描述（`@CSchema`/`@CParameter`/存量 `@ApiModelProperty`/`@ApiParam`）时不覆盖 |
| 空分组清理 | `CEmptyTagBeanPostProcessor` | 见「已知限制」第一条 |
| springfox 适配 | `COpenApi2Configuration#cSpringfoxHandlerProviderBeanPostProcessor` | 过滤含 `PatternParser` 的 mapping，规避 springfox 空指针 |

复用工具：

- `CSpringFoxUtils`：Docket 构建、请求头定义转全局参数；
- `CTextEnumUtils`：枚举允许值（可提交枚举名）与 text 说明生成；
- `CModelPropertyAnnotationUtils`：model 属性注解查找（annotatedElement 优先，其次 beanPropertyDefinition）。

## 配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `doc.openapi2.pathMapping` | `/` | 路径映射，一般是 nginx 做了反向代理、knife4j 检测不到路径时使用 |

## 已知限制

- **springfox 的分组名由控制器类名硬编码**：`WebMvcRequestHandler#groupName()` 直接调用
  `ControllerNamingUtils.controllerNameAsGroup(handlerMethod)`（类名 splitCamelCase("-") 转小写，如
  `WeComController` → `we-com-controller`），**不经过 `ResourceGroupingStrategy`**，注解与插件都改不了；
  而接口自身的 tag 由 `@CTag` 决定。两者不一致时，分组列表会多出「有分组名、无任何接口」的英文空分组
  （swagger-ui 上表现为中文分组 + 英文空分组并存）。
  `CEmptyTagBeanPostProcessor` 在文档模型生成后清除无人引用的分组声明来消除空分组（只删空分组，
  接口与其 tag 不动；knife4j 关闭文档时无该 Bean，不介入）。该处理器依赖 `ServiceModelToSwagger2Mapper`
  与 swagger-models 的模型结构，升级 springfox/knife4j 大版本需回归。
  **注意**：清理以「接口是否引用」为准，因此业务用 `Docket.tags(...)` 主动声明但暂无接口的**占位分组**也会被清除
  （无任何接口即 `paths` 为空时不清理，避免误判）。该行为有单测固化。
- 本模块基于 springfox（OpenAPI2，已停止维护）；Knife4j 4.x 建议迁移 OpenAPI3（springdoc-openapi），
  属模块级迁移，需排期。
- 插件与 Docket 目前**无总开关**（仅 `Docket` 支持业务覆盖）：生产环境用 knife4j 关文档时，本模块仍会装配
  插件与 Docket（无副作用，但无法一键关闭）。

## 依赖

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `ctool4j-web` | compile | Web 通用能力、请求头枚举 `CRequestHeaderEnum`、校验注解 |
| `knife4j-openapi2-spring-boot-starter` | compile | OpenAPI2 文档框架 |
| `ctool4j-definition` / `ctool4j-core` | 经 `ctool4j-web` 传递 | 文档描述注解、工具类 |
| `springfox-*` / `io.swagger` | 经 `knife4j` 传递 | 插件 SPI 与文档模型（源码中直接使用，属上游传递提供） |

## 相关入口

- 模块族总览：[ctool4j-doc-pom README](../README.md)
- 功能级设计文档：[doc/design/definition/openapi-doc-annotations.adoc](../../../../doc/design/definition/openapi-doc-annotations.adoc)
- 模块间依赖关系：[doc/dependency.adoc](../../../../doc/dependency.adoc)
