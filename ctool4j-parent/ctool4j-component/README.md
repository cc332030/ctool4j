# ctool4j-component

> 组件模块聚合：20 个组件模块的容器，并按层组织（基础层 → Spring 层 → 应用层 → 中间件层 → 配套层）。

## 简介

`ctool4j-component` 是全部组件模块（可被业务项目直接引入的构件）的聚合父级。它本身是聚合 pom，
除模块结构外只把一批**使用方需自行提供**的依赖以 provided/optional 下发给全部组件模块（见下表），
避免各组件重复声明。它同时是 `@Bean` 命名等组件约定的落点。

## 模块结构

| 层 | 模块 | 定位 |
|----|------|------|
| 基础层 | [ctool4j-definition](ctool4j-definition/README.md) | 基础定义：统一返回体、实体基类、枚举、注解、函数式接口 |
| | [ctool4j-core](ctool4j-core/README.md) | 核心工具库：字符串 / 集合 / 日期 / JSON / 反射 / 异常 / 日志 / 校验 |
| | [ctool4j-http-pom](ctool4j-http-pom/README.md) | Servlet 容器抽象层与两侧适配：`ctool4j-http-base` / `-javax` / `-jakarta` |
| | [ctool4j-db](ctool4j-db/README.md) | SQL 拼接工具 |
| | [ctool4j-transaction](ctool4j-transaction/README.md) | 事务注解约定（`@CTransactional`） |
| Spring 层 | [ctool4j-spring-base-pom](ctool4j-spring-base-pom/README.md) | Spring 侧抽象契约与两侧适配：拦截器 / 响应体增强 / 配置属性 / cors / 安全错误响应 |
| | [ctool4j-spring-pom](ctool4j-spring-pom/README.md) | `ctool4j-spring` / `ctool4j-spring-security` / `ctool4j-spring-cloud` |
| 应用层 | [ctool4j-web](ctool4j-web/README.md) | Web MVC 通用能力：全局异常、跨域、JWT、请求日志、traceId |
| | [ctool4j-feign](ctool4j-feign/README.md) | Feign 增强：请求头传播、日志、自定义拦截器 |
| | [ctool4j-redis](ctool4j-redis/README.md) | Redis 操作、Redisson 分布式锁、限流、幂等 |
| | [ctool4j-cache](ctool4j-cache/README.md) | 二级缓存：本地缓存 + Redis，防击穿 / 防雪崩 |
| 认证层 | [ctool4j-auth-pom](ctool4j-auth-pom/README.md) | `ctool4j-auth-base` / `ctool4j-auth-spring` |
| 中间件层 | [ctool4j-log-pom](ctool4j-log-pom/README.md) | 日志：请求日志、traceId、MDC、日志级别热更新 |
| | [ctool4j-mybatis-pom](ctool4j-mybatis-pom/README.md) | MyBatis-Plus 增强：Mapper/Service 分层契约、分页、SQL 注入 |
| | [ctool4j-nacos-pom](ctool4j-nacos-pom/README.md) | Nacos：Feign 本地实例注册、配置中心（占位） |
| | [ctool4j-mq-pom](ctool4j-mq-pom/README.md) | 消息队列（占位） |
| | [ctool4j-job-pom](ctool4j-job-pom/README.md) | 定时任务：xxl-job 自动配置、任务参数注入 |
| 文件层 | [ctool4j-file-pom](ctool4j-file-pom/README.md) | 文件处理：CSV、Excel、MinIO 对象存储 |
| 配套层 | [ctool4j-doc-pom](ctool4j-doc-pom/README.md) | 接口文档：OpenAPI2（knife4j / springfox）增强 |
| | [ctool4j-test-pom](ctool4j-test-pom/README.md) | 测试支撑：共享测试模型（部分占位） |

## 公共约定（本模块统一下发）

| 依赖 | 作用域 | 说明 |
|------|--------|------|
| `spring-boot-starter-web` | provided | 组件按需使用、由使用方提供 |
| `spring-boot-starter-validation` | provided | 参数校验组件 |
| `spring-boot-starter-logging` | provided | 日志门面与实现分离 |
| `spring-cloud-commons` / `spring-cloud-context` | provided | Spring Cloud 基础 API |
| `guava` | optional | 可选工具库（使用方按需引入） |
| `mapstruct` | provided | 编译期映射 |

组件模块**不得重复声明**上述依赖，也不得重复声明已在直接/间接依赖中的模块（判定见 [doc/dependency.adoc](../../doc/dependency.adoc)）。

## 相关入口

- 项目根索引：[README](../../README.adoc)（模块全景与各模块 README）
- 模块间依赖关系：[doc/dependency.adoc](../../doc/dependency.adoc)
