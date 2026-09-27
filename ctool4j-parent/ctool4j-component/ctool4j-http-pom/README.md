# ctool4j-http-pom

Servlet 侧的**容器抽象层与两侧适配**模块集：把「Servlet 类型」收敛到两侧同名类里，
让业务模块只依赖抽象层类型，从而在 `javax.servlet` 与 `jakarta.servlet` 之间切换容器时**业务代码零改动**。

规范依据见 `agent/AGENTS-PROJECT.MD` 的「javax/jakarta 双栈（抽象层 + 两侧同名适配）」一节。

## 子模块

| 模块 | 定位 | 内容 |
| --- | --- | --- |
| `ctool4j-http-base` | 抽象契约（容器无关，不引 servlet-api） | 请求/响应契约 `CHttpRequest` / `CHttpResponse`；过滤器契约 `CFilter` / `CFilterChain`；转发器契约 `CRequestDispatcher`；跨包异常 `CServletException` |
| `ctool4j-http-javax` | javax 侧适配（底层桥接，不含业务） | 契约的 javax 落地：`CHttpServletRequest` / `CHttpServletResponse` / `CFilterAdapter` / `CFilterChainAdapter` / `CRequestDispatcherAdapter` |
| `ctool4j-http-jakarta` | jakarta 侧适配（同名同构） | 与 `-javax` 同包同名，仅 Servlet 类型换成 `jakarta.*`（本模块不依赖 Spring，可直接登记构建） |

## 切换容器

把依赖的 `-javax` 模块换成 `-jakarta` 模块即可（两侧同包同名，业务代码与 import 不变）：

```xml
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-http-javax</artifactId>
</dependency>
```

两侧模块对 `servlet-api` 的依赖均声明为 `optional`：**不传递给使用方**，运行时由容器提供；
同时引入两个实现模块并不代表可在一个应用里同时生效。

## 使用约束（摘要）

- 业务模块**优先实现/使用 base 的契约**（`CHttpRequest` / `CHttpResponse` / `CFilter` / `CFilterChain` / `CRequestDispatcher`），对外签名不暴露某一容器包的类型。
- **不要直接使用两侧模块的类**，只有在「把容器对象转成抽象层对象」这一边界处出现（如 `CHttpServletResponse.of(response)`）。
- 抽象层只声明两边公共面与容器无关的异常（`CServletException`、`IOException`）：返回或入参落在 Servlet 包内的方法（会话、Cookie、二进制流、上传分片、协议升级）、以及两边取值不同的常量只能落在两侧同名类上。
- 两侧适配模块**只做类型转换**，不放业务逻辑；使用方按自身容器依赖引入其中一个。

## 构建与验证

```bash
# 构建本聚合下的全部模块（注意：-pl 指定聚合 pom 不会递归子模块）
./mvnw -f ctool4j-parent/ctool4j-component/ctool4j-http-pom/pom.xml install
```

改动 `-javax` 侧时须同步 `-jakarta` 侧：两侧 `src/main` 下的同名文件除 `javax`/`jakarta` 字样外应**完全一致**
（自查脚本见 `agent/AGENTS-PROJECT.MD`）。
