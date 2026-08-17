# ctool4j

Java 工具库，面向 Spring Boot 2.7 / Spring Cloud 2021 生态，提供 Web、缓存、数据库、日志、消息、任务、文件等场景的组件化封装与约定式开发支持。

## 模块全景

项目采用「构建基础设施 + 组件模块」两层结构：

```
ctool4j（根，packaging=pom）
├── ctool4j-dependencies     BOM：统一管理 ctool4j 自研模块版本
├── ctool4j-bom              BOM：统一管理第三方依赖版本（含 import 上面的 dependencies）
├── ctool4j-processor-pom    编译期注解处理器（代码生成）
└── ctool4j-parent
    └── ctool4j-component
        ├── 基础层    definition / core / db / transaction
        ├── Spring 层 spring-pom（spring / spring-security / spring-cloud）
        ├── 应用层    web / feign / redis / cache
        ├── 中间件层  log-pom / mybatis-pom / nacos-pom / mq-pom / job-pom
        ├── 文件层    file-pom（csv / excel / minio）
        └── 配套层    doc-pom / test-pom
```

## 模块依赖关系

基础依赖链自底向上为：`definition → core → spring →（web / redis / ...）`。

```
ctool4j-definition（基础定义，无依赖）
        │
        ▼
ctool4j-core（核心工具库，依赖 definition）
        │
        ▼
ctool4j-spring（Spring 基础设施，依赖 core + autowired-processor）
        │
        ├──► ctool4j-spring-cloud（依赖 spring）
        ├──► ctool4j-web（依赖 spring）
        │        └──► ctool4j-spring-security（依赖 web）
        ├──► ctool4j-redis（依赖 spring）
        │        └──► ctool4j-cache（依赖 redis）
        └──► ctool4j-transaction（依赖 spring）

ctool4j-core ──► ctool4j-db

ctool4j-spring-cloud + ctool4j-log-base ──► ctool4j-feign

ctool4j-log-base（依赖 spring / web / spring-cloud / core）
ctool4j-mybatis-base（依赖 core / definition / spring / redis）
ctool4j-job-base（依赖 spring）
ctool4j-nacos-discovery（依赖 spring）
```

## 模块文档索引

| 模块 | 定位 | 文档 |
|------|------|------|
| ctool4j-definition | 基础定义：统一返回体、实体基类、枚举、注解、函数式接口 | [README](ctool4j-parent/ctool4j-component/ctool4j-definition/README.md) |
| ctool4j-core | 核心工具库：字符串/集合/日期/JSON/反射/异常/日志/校验 | [README](ctool4j-parent/ctool4j-component/ctool4j-core/README.md) |
| ctool4j-db | SQL 拼接工具 | [README](ctool4j-parent/ctool4j-component/ctool4j-db/README.md) |
| ctool4j-transaction | 事务注解约定（@CTransactional） | [README](ctool4j-parent/ctool4j-component/ctool4j-transaction/README.md) |
| ctool4j-spring-pom | Spring 基础设施：Bean 装配、Context 工具、Security、Cloud | [README](ctool4j-parent/ctool4j-component/ctool4j-spring-pom/README.md) |
| ctool4j-web | Web MVC 通用能力：全局异常、跨域、JWT、请求日志、traceId 透传 | [README](ctool4j-parent/ctool4j-component/ctool4j-web/README.md) |
| ctool4j-feign | Feign 增强：请求头传播、日志、自定义拦截器 | [README](ctool4j-parent/ctool4j-component/ctool4j-feign/README.md) |
| ctool4j-redis | Redis 操作与 Redisson 分布式锁 | [README](ctool4j-parent/ctool4j-component/ctool4j-redis/README.md) |
| ctool4j-cache | 二级缓存：本地缓存 + Redis，防击穿/防雪崩 | [README](ctool4j-parent/ctool4j-component/ctool4j-cache/README.md) |
| ctool4j-log-pom | 日志：请求日志、traceId 透传、MDC、日志级别热更新 | [README](ctool4j-parent/ctool4j-component/ctool4j-log-pom/README.md) |
| ctool4j-mybatis-pom | MyBatis-Plus 增强：Mapper/Service 分层契约、分页、SQL 注入 | [README](ctool4j-parent/ctool4j-component/ctool4j-mybatis-pom/README.md) |
| ctool4j-nacos-pom | Nacos：Feign 本地实例注册（配置中心为占位） | [README](ctool4j-parent/ctool4j-component/ctool4j-nacos-pom/README.md) |
| ctool4j-mq-pom | 消息队列（占位，待实现） | [README](ctool4j-parent/ctool4j-component/ctool4j-mq-pom/README.md) |
| ctool4j-job-pom | 定时任务：xxl-job 自动配置、任务参数注入、时间计算 | [README](ctool4j-parent/ctool4j-component/ctool4j-job-pom/README.md) |
| ctool4j-file-pom | 文件处理：CSV、Excel、MinIO 对象存储 | [README](ctool4j-parent/ctool4j-component/ctool4j-file-pom/README.md) |
| ctool4j-doc-pom | 接口文档：OpenAPI2（knife4j/springfox）增强 | [README](ctool4j-parent/ctool4j-component/ctool4j-doc-pom/README.md) |
| ctool4j-test-pom | 测试支撑：共享测试模型（部分占位） | [README](ctool4j-parent/ctool4j-component/ctool4j-test-pom/README.md) |

## 构建基础设施

- **ctool4j-dependencies**：BOM，统一声明全部 ctool4j 自研构件的版本。
- **ctool4j-bom**：BOM，统一管理第三方依赖版本（Spring Boot / Spring Cloud / MyBatis-Plus / Redis / MQ / 工具库等），并 import `ctool4j-dependencies`。业务项目引入该 BOM 即可获得一致的依赖版本，无需手写版本号。
- **ctool4j-processor-pom**：编译期注解处理器，通过 SPI 自动注册，被组件模块用作 annotation processor：
  - `ctool4j-autowired-processor`：处理 `@CAutowired` / `@CAutowiredScan`，为静态字段生成构造器注入类（解决 Spring 静态工具类无法 `@Autowired` 的问题）。
  - `ctool4j-mybatis-processor`：处理 `@CAutoBizService`，根据业务实体接口的 getter 自动生成业务 Service 接口（含按业务 ID 的增删改查 default 方法）。
  - `ctool4j-mq-processor`：占位，待实现。

## 使用说明

各模块使用方式详见上表对应的模块 README。业务项目建议通过 `ctool4j-bom` 统一引入依赖版本。

```cmd
# 推送所有 tag
git push gitlab --tags
```
