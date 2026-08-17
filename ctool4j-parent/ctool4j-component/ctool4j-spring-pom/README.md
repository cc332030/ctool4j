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
- 工具类：`CSpringUtils`（Spring 容器）、`CRequestUtils`（请求）、`CFileUtils`（文件）、`CRestTemplateUtils`、`CAnnotationUtils`（注解）、`CAspectUtils`（切面）、`CAutowiredUtils`
- 生命周期：应用启动完成后执行 `CStartedApplicationRunner`、初始化回调 `ICSpringInit`
- 异常忽略记录：`@CLogAndIgnoreThrowable` + 切面
- Jackson 与 Spring 全局初始化（`CJacksonInit` / `CSpringInit`）
- 测试组合注解 `@CTool4jSpringBootTest`

### 包结构

| 包 | 用途 |
|----|------|
| `annotation` | 组合注解：`@CSpringBootApplication` / `@CLazyService` / `@CAutowired` |
| `config` | 配置属性：应用配置 `CSpringApplicationConfig`、Jackson 配置 |
| `configuration` | Spring / Jackson 初始化装配 |
| `bean` | 配置 Bean 持有容器 |
| `boot` | 启动后运行器 |
| `interfaces` / `lifecycle` | 有序执行接口 `ICOrdered`、初始化回调 |
| `util` | 容器 / 请求 / 文件 / HTTP / 注解 / 切面工具 |
| `exception.annotation` / `exception.aspect` | 异常忽略与记录 |
| `test.annotation` | 测试组合注解 |

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CSpringBootApplication` | 注解 | Spring Boot 启动组合注解 |
| `CAutowired` / `CAutowiredScan` | 注解 | 自定义注入注解（编译期生成静态字段注入） |
| `CSpringUtils` | 工具类 | Spring 容器获取、Bean 操作 |
| `CRequestUtils` | 工具类 | HttpServletRequest 操作（header / ip / 参数） |
| `CFileUtils` | 工具类 | 文件读写工具 |
| `CAnnotationUtils` | 工具类 | 注解扫描与读取 |
| `CAspectUtils` | 工具类 | 切面操作工具 |
| `CStartedApplicationRunner` | 运行器 | 应用启动完成后执行 |
| `ICSpringInit` | 接口 | Spring 初始化回调接口 |
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
- JWT 认证过滤器基类 `CAbstractJwtFilter`（业务继承实现 token 校验）
- 未认证 / 拒绝访问 / 会话过期统一处理（JSON 返回）
- 用户认证服务抽象：`ICUserDetailsService` / `ICAuthenticationUserDetailsService` / `ICUserDetailsPasswordService`
- 安全上下文与认证工具：`CSpringSecurityUtils` / `CAuthenticationUtils`
- 放行 / 禁止路径配置（`ICRequestMatchersConfig`）
- 安全用户模型 `CSecurityUser`

### 核心类

| 类 | 类型 | 职责 |
|----|------|------|
| `CSecurityConfiguration` | 配置 | SecurityFilterChain 装配与初始化 |
| `CAbstractJwtFilter` | 过滤器 | JWT 认证过滤器基类 |
| `CAuthenticationEntryPoint` | 处理器 | 未认证统一处理 |
| `CAccessDeniedHandler` | 处理器 | 拒绝访问统一处理 |
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
| `spring-session-data-redis`（provided） | Redis Session |
| `spring-boot-starter-data-redis`（provided） | Redis |

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

## 模块选择建议

- 仅用 Spring 基础能力 → 引入 `ctool4j-spring`
- 需要登录认证 → 追加 `ctool4j-spring-security`
- 微服务场景 → 追加 `ctool4j-spring-cloud`（及 feign / nacos 等）
