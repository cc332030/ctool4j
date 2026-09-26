# ctool4j-spring-pom

> Spring 基础设施聚合模块，包含 3 个子模块：`ctool4j-spring`、`ctool4j-spring-security`、`ctool4j-spring-cloud`。

## 简介

`ctool4j-spring-pom` 是聚合 pom，无自身源码。三个子模块分别提供：Spring 基础装配与工具、Spring Security + JWT 认证授权、Spring Cloud 微服务能力。

---

## 子模块一：ctool4j-spring

> Spring 生态的基础工具与初始化装配模块，提供依赖注入、切面、HTTP/请求、文件等通用能力。

### 功能特性

- 自定义依赖注入 `@CAutowired` / `@CAutowiredScan`（由 `ctool4j-autowired-processor` 编译期生成注入代码，支持静态字段注入）
- Spring Boot 启动组合注解 `@CSpringBootApplication`、懒加载 `@CLazyService`
- 配置属性绑定注解 `@CConfigurationProperties`（`@ConfigurationProperties` 的组合注解，只承载前缀 `value`；两个绑定开关都在元注解上给定，默认忽略多出的未知键与类型不匹配的属性，都不报错、不中断启动；需要强校验时改用元注解）
- 工具类：`CSpringUtils`（Spring 容器）、`CRequestUtils`（请求）、`CFileUtils`（文件）、`CRestTemplateUtils`、`CAnnotationUtils`（注解）、`CAspectUtils`（切面）、`CAutowiredUtils`
- 代理解析：`CProxyUtils`（代理判定与真实业务类解析；热路径按「非 Spring 代理快速返回 + 惰性解包装」解析，单次成本在纳秒量级）
- 生命周期：应用启动完成后由 `CStartedApplicationRunner` 执行（回调 `ICStarted#onStarted`，并清理本模块的启动期缓存）、初始化回调 `ICSpringInit`
- 语义接口：`ICRealClass`（真实业务类/类名/包名契约，带默认实现，代理场景下 `getClass()` 取到代理类时用）
- 异常忽略记录：`@CLogAndIgnoreThrowable` + 切面
- Jackson 与 Spring 全局初始化（`CJacksonInit` / `CSpringInit`）
- 测试组合注解 `@CTool4jSpringBootTest`

### 包结构

| 包 | 用途 |
|----|------|
| `annotation` | 组合注解：`@CSpringBootApplication` / `@CLazyService` / `@CConfigurationProperties` / `@CAutowired` |
| `config` | 配置属性：应用配置 `CSpringApplicationConfig`、Jackson 配置 |
| `configuration` | Spring / Jackson 初始化装配 |
| `bean` | 配置 Bean 持有容器 |
| `boot` | 启动后运行器 |
| `interfaces` / `lifecycle` | 有序执行接口 `ICOrdered`、真实业务类契约 `ICRealClass`、启动完成回调 `ICStarted`、初始化回调 |
| `util` | 容器 / 请求 / 文件 / HTTP / 注解 / 切面 / 代理工具 |
| `exception.annotation` / `exception.aspect` | 异常忽略与记录 |
| `test.annotation` | 测试组合注解 |

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CSpringBootApplication` | 注解 | Spring Boot 启动组合注解 |
| `CConfigurationProperties` | 注解 | 配置属性绑定组合注解（只承载前缀；默认忽略未匹配属性与非法值由元注解给定） |
| `CAutowired` / `CAutowiredScan` | 注解 | 自定义注入注解（编译期生成静态字段注入） |
| `CSpringUtils` | 工具类 | Spring 容器获取、Bean 操作 |
| `CRequestUtils` | 工具类 | Servlet 请求操作（header / ip / 参数）；置于 `ctool4j-spring-servlet`（模块内按档位切换容器侧），容器无关部分在 `ctool4j-spring-base` 的 `CHttpRequestUtils`（调用复用、不继承），本类只留取当前请求/响应等接触容器的入口 |
| `CFileUtils` | 工具类 | 文件读写工具 |
| `CAnnotationUtils` | 工具类 | 注解扫描与读取 |
| `CAspectUtils` | 工具类 | 切面操作工具 |
| `CProxyUtils` | 代理解析类 | 代理判定（Spring AOP / JDK 动态代理）与真实业务类/类名/包名解析；单次调用纳秒量级、不缓存解析结果 |
| `CStartedApplicationRunner` | 运行器 | 应用启动完成后输出启动成功日志，并回调 `ICStarted#onStarted` |
| `ICStarted` | 接口 | 应用启动完成回调接口（`SpringApplication.run` 执行完成后触发） |
| `ICSpringInit` | 接口 | Spring 初始化回调接口（单例实例化完成后触发） |
| `ICRealClass` | 接口 | 真实业务类/类名/包名契约，带默认实现（代理场景下由业务基类实现） |
| `CLogAndIgnoreThrowable` | 注解 | 忽略并记录异常 |
| `CTool4jSpringBootTest` | 注解 | 测试组合注解 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-core` | 核心工具 |
| `ctool4j-autowired-processor` | 编译期注入代码生成 |
| `spring-boot-starter` | Spring Boot 基础 |

---

## 子模块二：ctool4j-spring-security

> Spring Security + JWT + Redis Session 的认证授权装配模块。

### 功能特性

- 自动装配 `SecurityFilterChain`（`CSecurityConfiguration`）
- 安全过滤器链装配时按类型契约 `CAbstractWebAuthFilter`（`ctool4j-spring-servlet`，模块内按档位切换容器侧）注入认证过滤器，实现由 `ctool4j-auth-*` 模块提供（本模块不再提供过滤器基类）
- 未认证 / 拒绝访问 / 会话过期统一处理（JSON 返回）
- 用户认证服务抽象：`ICUserDetailsService` / `ICAuthenticationUserDetailsService` / `ICUserDetailsPasswordService`
- 安全上下文与认证工具：`CSpringSecurityUtils` / `CAuthenticationUtils`
- 放行 / 禁止路径配置（`ICRequestMatchersConfig`）
- 安全用户模型 `CSecurityUser`

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CSecurityConfiguration` | 配置 | SecurityFilterChain 装配与初始化 |
| `CAuthenticationEntryPoint` | 处理器 | 未认证统一处理（置于 `ctool4j-spring-security-servlet`，模块内按档位切换容器侧） |
| `CAccessDeniedHandler` | 处理器 | 拒绝访问统一处理（置于 `ctool4j-spring-security-servlet`，模块内按档位切换容器侧） |
| `CSessionInformationExpiredStrategy` | 处理器 | 会话过期处理 |
| `ICUserDetailsService` | 接口 | 用户认证服务抽象 |
| `CSecurityUser` | 模型 | 安全用户模型 |
| `CSpringSecurityUtils` | 工具类 | 安全上下文工具 |
| `CEmptyUserDetailService` | 实现 | 空实现兜底 UserDetailsService |
| `CSpringSecurityRequestMatchersPathConfig` | 配置 | 放行/禁止路径配置项 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-web` | Web 通用能力 |
| `spring-boot-starter-security` | Security |
| `spring-session-data-redis`（optional） | Redis Session |
| `spring-boot-starter-data-redis`（optional） | Redis |

---

## 子模块三：ctool4j-spring-cloud

> Spring Cloud 通用能力最小装配模块。

### 功能特性

- Spring Cloud 上下文初始化（`CToolSpringCloudConfiguration` / `CToolSpringCloudInit`）

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CToolSpringCloudConfiguration` | 配置 | Spring Cloud 装配 |
| `CToolSpringCloudInit` | 初始化 | Spring Cloud 初始化 |

### 依赖

| 依赖 | 说明 |
|------|------|
| `ctool4j-spring` | Spring 基础设施 |
| `spring-cloud-commons` | Spring Cloud 基础 |

## 使用与配置（引用方）

引入坐标、用法示例、配置项、模块选型、误用点等**面向引用方**的内容：见使用文档 [`doc/use/spring.adoc`](../../../doc/use/spring.adoc)（整体开放为静态服务），本 README 不再重复（同一事实两个真源必然漂移）。

## 文档

本模块的**类级与方法级内容由源码 javadoc 承载**；功能级/跨类设计文档与模块间依赖见下。

| 内容 | 文档 |
|------|------|
| 代理场景取真实业务类/类名/包名（设计取舍与适用边界） | [doc/design/spring/real-class-name.adoc](../../../doc/design/spring/real-class-name.adoc) |
| IP 相关能力设计 | [doc/design/spring/ip.adoc](../../../doc/design/spring/ip.adoc) |
| 公共流程热路径的性能实测记录 | [doc/design/spring/spring-perf.adoc](../../../doc/design/spring/spring-perf.adoc) |
| 模块间依赖关系（全项目唯一视图） | [doc/dependency.adoc](../../../doc/dependency.adoc) |
| 文档目录导航入口 | [doc/README.adoc](../../../doc/README.adoc) |
