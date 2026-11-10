# ctool4j-spring-base-pom

Spring 侧的**容器抽象层与容器适配**模块集：把「Spring 扩展点签名里带的 Servlet 类型」收敛到适配层的同名类里，
让业务模块只依赖抽象层类型，从而在 `javax.servlet` 与 `jakarta.servlet` 之间切换容器时**业务代码零改动**。

规范依据见 `agent/AGENTS-PROJECT.MD` 的「javax/jakarta 双栈（抽象层 + 容器适配）」一节。

## 子模块

| 模块 | 定位 | 内容 |
| --- | --- | --- |
| `ctool4j-spring-base` | 抽象契约（容器无关）+ Spring 侧容器适配 | 基层契约 `ICHandlerInterceptor`、`ICResponseBodyAdvice`；`CConfigurationProperties`（配置属性组合注解）；cors 配置与工具（`CCorsConfig`、`CCorsOriginConfig`、`CCorsUtils`）；`CResourceUrlConstants`；适配层同名类**调用**的公共工具：`CHttpRequestUtils`（请求工具的容器无关部分）、`CErrorUtils`（错误响应组装）、`CResourceUrlUtils`（静态资源忽略判定）。容器落地在**模块内的档位源目录**：`src/main/java-jdk8` 承 javax、`src/main/java-latest` 承 jakarta（同包同名成对镜像：`ICSpringHandlerInterceptor`、`ICSpringResponseBodyAdvice`、`ICFilter`、`CAbstractWebAuthFilter`、`CCorsFilter`、`CResourceFilter`、`CRequestUtils`、`CErrorController`），按 JDK 档位追加其一 |
| `ctool4j-spring-security-base` | 安全侧抽象契约 + 容器适配 | 抽象契约：`CSecurityResponseUtils`（只依赖抽象层响应的统一 JSON 错误写出）、`CUnauthorizedHandler` / `CForbiddenHandler` 与默认实现。容器落地在档位源目录（同包同名成对镜像）：`CAuthenticationEntryPoint`、`CAccessDeniedHandler`（Spring Security 定签名）、`CSecurityResponseWriter`，按 JDK 档位追加其一 |

## 切换容器

**不需要换依赖模块**：`ctool4j-spring-base` 与 `ctool4j-spring-security-base` 内已同时容纳
javax 与 jakarta 两套同包同名源码，由 JDK 档位（`JDK_VERSION`）自动选用——jdk8 档位读 `src/main/java-jdk8`
（`javax.servlet`），其余档位读 `src/main/java-latest`（`jakarta.servlet`）。业务代码与依赖坐标都不变。

两档位对应不同的 Spring 矩阵（jdk8 → Spring 5.3 / Boot 2.7；其余 → Spring 6+ / Boot 3+），由版本目录按档位给值，
故不再有"某一侧未登记进构建"的情形。

## 使用约束（摘要）

- 业务模块**优先使用 base 的抽象类型**（`CHttpRequest` / `CHttpResponse` / `CFilterChain` …），对外签名不暴露某一容器包的类型。
- **不要直接使用适配层的类**，只有在「把容器对象转成抽象层对象」这一边界处出现（如 `CHttpServletResponse.of(response)`）。
- 无容器依赖的类、常量、注解一律放 base 公共源目录；两侧**取值不同**的常量只能落在适配层的同名类上。
- 适配层同名类**只留必须接触容器的部分**（包装容器对象、被框架锁死的签名、两侧取值不同的常量）；不接触容器的算法与分支抽到 base 的公共类
  （如 `CHttpRequestUtils`、`CErrorUtils`、`CResourceUrlUtils`），两侧同名类**调用**它（工具类之间不继承），公共实现只存一份、两侧不复制。
- 抽象层方法只声明容器无关的异常（`CServletException`、`IOException`），不带入 `ServletException`。

## 构建与验证

```bash
# 构建本聚合下的全部已登记模块（Gradle 项目依赖：上游模块会被自动构建）
gradle build
```
