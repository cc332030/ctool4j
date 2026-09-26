# ctool4j-http-pom

Servlet 侧的**容器抽象层与容器适配**模块集：把「Servlet 类型」收敛到适配层的同名类里，
让业务模块只依赖抽象层类型，从而在 `javax.servlet` 与 `jakarta.servlet` 之间切换容器时**业务代码零改动**。

规范依据见 `agent/AGENTS-PROJECT.MD` 的「javax/jakarta 双栈（抽象层 + 容器适配）」一节。

## 子模块

| 模块 | 定位 | 内容 |
| --- | --- | --- |
| `ctool4j-http-base` | 抽象契约（容器无关，不引 servlet-api） | 请求/响应契约 `CHttpRequest` / `CHttpResponse`；过滤器契约 `CFilter` / `CFilterChain`；转发器契约 `CRequestDispatcher`；跨包异常 `CServletException` |
| `ctool4j-http-servlet` | 容器适配（底层桥接，不含业务） | 契约的容器落地：`CHttpServletRequest` / `CHttpServletResponse` / `CFilterAdapter` / `CFilterChainAdapter` / `CRequestDispatcherAdapter`。**javax 与 jakarta 两套源码同包同名、同在一个模块内**，按 JDK 档位选用 `src/main/java-javax` 或 `src/main/java-jakarta`，不再拆成两个模块 |

## 切换容器

**不需要换依赖模块**：`ctool4j-http-servlet` 内已同时容纳 javax 与 jakarta 两套同包同名源码，
由 JDK 档位（`JDK_VERSION`）自动选用——jdk8 档位读 `src/main/java-javax`（`javax.servlet`），
其余档位读 `src/main/java-jakarta`（`jakarta.servlet`）。业务代码与依赖坐标都不变。

```xml
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-http-servlet</artifactId>
</dependency>
```

本模块对 `servlet-api` 的依赖按档位取自版本目录（jdk8 → `javax.servlet:javax.servlet-api`，
其余 → `jakarta.servlet:jakarta.servlet-api`），声明为 `optional`：**不传递给使用方**，运行时由容器提供。

## 使用约束（摘要）

- 业务模块**优先实现/使用 base 的契约**（`CHttpRequest` / `CHttpResponse` / `CFilter` / `CFilterChain` / `CRequestDispatcher`），对外签名不暴露某一容器包的类型。
- **不要直接使用适配层的类**，只有在「把容器对象转成抽象层对象」这一边界处出现（如 `CHttpServletResponse.of(response)`）。
- 抽象层只声明两边公共面与容器无关的异常（`CServletException`、`IOException`）：返回或入参落在 Servlet 包内的方法（会话、Cookie、二进制流、上传分片、协议升级）、以及两边取值不同的常量只能落在适配层的同名类上。
- 容器适配层**只做类型转换**，不放业务逻辑；使用方按自身容器依赖引入。

## 构建与验证

```bash
# 构建本聚合下的全部模块（注意：-pl 指定聚合 pom 不会递归子模块）
./mvnw -f ctool4j-parent/ctool4j-component/ctool4j-http-pom/pom.xml install
```

两套源码同包同名、互为镜像：`src/main/java-javax` 与 `src/main/java-jakarta` 下的同名文件除 `javax`/`jakarta`
字样外应**完全一致**（规范见 `agent/AGENTS-PROJECT.MD`「javax/jakarta 双栈」）。

```bash
# 两档位各构建一次，即可确认两侧源码都被编译到
gradle -DJDK_VERSION=8  :ctool4j-http-servlet:build
gradle -DJDK_VERSION=25 :ctool4j-http-servlet:build
```
