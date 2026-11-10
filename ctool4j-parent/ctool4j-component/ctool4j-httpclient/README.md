# ctool4j-httpclient

HTTP 客户端工具模块：全局复用的 `CHttpClientUtils`（超时常量、连接池、长连接策略、`HttpClient`
与 Spring `ClientHttpRequestFactory` 常量），供 `RestTemplate` / `WebClient` 等统一超时与连接池配置。

## 为什么独立成模块

HttpClient 4（`org.apache.http`）与 HttpClient 5（`org.apache.hc.client5`）的包名空间与 API 全然不同，
且 Spring 7（Boot 4 基线）的 `HttpComponentsClientHttpRequestFactory` 只接受 HC5——整类不可调和，
只能按档位各留一份同包同名实现。原先这份实现落在 `ctool4j-core`（`java-jdk8` / `java-latest` 追加形），
是该模块唯一被档位分流的类；拆出独立模块后，`ctool4j-core` 的主源集保持单源，
而 HTTP 客户端的使用方（`ctool4j-spring` 的 `CRestTemplateUtils` 等）显式引入本模块。

| 档位 | 源目录 | HttpClient |
| --- | --- | --- |
| 最新 LTS | `src/main/java` | HttpClient 5（版本由 Boot 4 BOM 提供） |
| jdk8 | `src/main/java-jdk8` | HttpClient 4（4.5.14 末版，Apache 已归档、Boot 4 亦不再管理，故显式钉版） |

两份源码**同包同名**（`com.c332030.ctool4j.core.util.CHttpClientUtils`，包名保留原位以兼容既有 import），
按 JDK 档位**整体替换**（`cExclusiveJdk8SourceDirs`，见根 `build.gradle.kts`）；对外常量名与语义完全一致。

## 依赖约定

- `api(lib("httpclient"))`：按档位取 HC4 / HC5，**api 传递**——`HTTP_CLIENT` / `REQUEST_CONFIG` 等
  公共常量的类型就是 HC 类型，使用方编译即需。
- spring-web 类型（`ClientHttpRequestFactory`）不在此声明：由根脚本对 component 档位注入的
  compileOnly（`spring-boot-starter-web`）提供，与该类在 `ctool4j-core` 时期的取法一致。

## 使用

```java
// 直接使用常量
HttpClient client = CHttpClientUtils.HTTP_CLIENT;

// 构建连接池化的 RestTemplate（ctool4j-spring 的 CRestTemplateUtils 即这样组装）
RestTemplate restTemplate = new RestTemplate(CHttpClientUtils.REQUEST_FACTORY);
```

依赖坐标：

```xml
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-httpclient</artifactId>
</dependency>
```

## 构建与验证

```bash
# jdk8 档位（HC4 侧）
JAVA_HOME=$HOME/.jdks/temurin-25.0.4.1 ./gradlew :ctool4j-httpclient:build -DJDK_VERSION=8

# 最新 LTS 档位（HC5 侧）
JAVA_HOME=$HOME/.jdks/temurin-25.0.4.1 ./gradlew :ctool4j-httpclient:build
```

相关文档：[ctool4j-core](../ctool4j-core)、[组件总览](../README.md)、[依赖关系](../../../doc/dependency.adoc)、
[兼容性说明](../../../doc/compatibility.adoc)。
