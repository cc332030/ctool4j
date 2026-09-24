# ctool4j-spring-base-pom

Spring 侧的**容器抽象层与两侧适配**模块集：把「Spring 扩展点签名里带的 Servlet 类型」收敛到两侧同名类里，
让业务模块只依赖抽象层类型，从而在 `javax.servlet` 与 `jakarta.servlet` 之间切换容器时**业务代码零改动**。

规范依据见 `agent/AGENTS-PROJECT.MD` 的「javax/jakarta 双栈（抽象层 + 两侧同名适配）」一节。

## 子模块

| 模块 | 定位 | 内容 |
| --- | --- | --- |
| `ctool4j-spring-base` | 抽象契约（容器无关） | 基层契约 `ICHandlerInterceptor`、`ICResponseBodyAdvice`；`CConfigurationProperties`（配置属性组合注解）；cors 配置与工具（`CCorsConfig`、`CCorsOriginConfig`、`CCorsUtils`）；`CResourceUrlConstants` |
| `ctool4j-spring-javax` | javax 侧适配 | `ICSpringHandlerInterceptor`、`ICSpringResponseBodyAdvice`（侧同名桥接接口）；`CRequestUtils`（依赖 Spring 的 `ServletRequestAttributes`）；`ICFilter`、`CAbstractWebAuthFilter`、`CResourceFilter`、`CErrorController`；cors 过滤器/拦截器/advice |
| `ctool4j-spring-jakarta` | jakarta 侧适配（同名同构） | 与 `-javax` 同包同名，仅 Servlet 类型换成 `jakarta.*`。**当前未登记进构建**（需 Spring 6 / Boot 3 依赖矩阵） |
| `ctool4j-spring-security-base` | 安全侧抽象契约 | `CSecurityResponseUtils`（只依赖抽象层响应的统一 JSON 错误写出）、`CUnauthorizedHandler` / `CForbiddenHandler` 与默认实现 |
| `ctool4j-spring-security-javax` | 安全侧 javax 适配 | `CAuthenticationEntryPoint`、`CAccessDeniedHandler`（Spring Security 定签名）、`CSecurityResponseWriter`、`CSpringSecurityUtils` |
| `ctool4j-spring-security-jakarta` | 安全侧 jakarta 适配（同名同构） | 同上，类型换 `jakarta.*`。**当前未登记进构建** |

## 切换容器

把依赖的 `-javax` 模块换成 `-jakarta` 模块即可（两侧同包同名，业务代码与 import 不变）。

`-jakarta` 侧需要 Spring 6 的 Servlet 支持（如 `ServletServerHttpRequest#getServletRequest()` 返回 jakarta 类型），
在 Spring 5.3 / Boot 2.x 矩阵下无法编译，因此**未登记**进本聚合模块的 `<modules>`；
升级到 Spring 6 / Boot 3 后把两个 `-jakarta` 模块加回 `pom.xml` 的 `<modules>` 即可。

## 使用约束（摘要）

- 业务模块**优先使用 base 的抽象类型**（`CHttpRequest` / `CHttpResponse` / `CFilterChain` …），对外签名不暴露某一容器包的类型。
- **不要直接使用两侧模块的类**，只有在「把容器对象转成抽象层对象」这一边界处出现（如 `CHttpServletResponse.of(response)`）。
- 无容器依赖的类、常量、注解一律放 base；两侧**取值不同**的常量只能落在两侧同名类上。
- 抽象层方法只声明容器无关的异常（`CServletException`、`IOException`），不带入 `ServletException`。

## 构建与验证

```bash
# 构建本聚合下的全部已登记模块（注意：-pl 指定聚合 pom 不会递归子模块）
./mvnw -f ctool4j-parent/ctool4j-component/ctool4j-spring-base-pom/pom.xml install
```
