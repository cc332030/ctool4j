# ctool4j-doc-pom

> 接口文档聚合模块：OpenAPI2（springfox / knife4j）文档增强（`ctool4j-doc-openapi2`）。

## 简介

`ctool4j-doc-pom` 是聚合 pom，包含子模块：

- `ctool4j-doc-openapi2`：OpenAPI2（springfox / knife4j）接口文档增强。

通用接口文档描述注解（`CSchema`/`CTag`/`COperation`/`CParameter`）定义在 `ctool4j-definition`
的 `com.c332030.ctool4j.doc.annotation` 包（设计文档见 `doc/design/definition/`），本模块插件读取后落地为 Swagger 文档。

## 子模块：ctool4j-doc-openapi2

> OpenAPI2（springfox / knife4j）接口文档增强：读取 `doc.annotation` 文档注解与 web 校验注解，生成 / 增强 Swagger 文档。

### 功能特性

- **Docket 自动装配**：`COpenApi2Configuration` 自动装配 Docket（收集标注 `@Api` 或 `@CTag` 的 Controller、注入全局请求头）并注册各文档插件
- **文档注解映射**：`@CTag` 分组、`@COperation` 摘要 / 说明、`@CParameter` 参数、`@CSchema` 属性描述，分别由 `CTagAnnotationPlugin`、`COperationAnnotationPlugin`、`CParameterAnnotationPlugin`、`CSchemaAnnotationModelPropertyPlugin` 落地
- **必填参数映射**：标注 `@CRequired` / `@NotEmpty` 的参数在文档中标记必填（`CRequiredAnnotationPlugin` / `CNotEmptyAnnotationPlugin`）
- **text 枚举展示**：实现 `ICText` 的枚举，在 model 属性 / 参数中以 description 展示 text（`CTextEnumModelPropertyPlugin` / `CTextEnumParameterPlugin`）
- **全局请求头**：`CSpringFoxUtils` 将请求头定义（`ICRequestHeader`，如 `CRequestHeaderEnum.AUTHORIZATION`）写入全局参数
- **兼容性修复**：修复 springfox 的 handlerMappings 空指针问题

### 依赖

| 依赖 | 说明 |
|------|------|
| `knife4j-openapi2-spring-boot-starter` | OpenAPI2 文档框架（springfox / knife4j / swagger） |
| `ctool4j-definition` | 接口文档描述注解（`doc.annotation` 包） |
| `ctool4j-web` | 请求头枚举 `CRequestHeaderEnum`、校验注解 `CRequired` / `CNotRequired` |
| `ctool4j-core`（经 `ctool4j-web` → `ctool4j-spring` 传递） | 请求头接口 `ICRequestHeader`、工具类 |
