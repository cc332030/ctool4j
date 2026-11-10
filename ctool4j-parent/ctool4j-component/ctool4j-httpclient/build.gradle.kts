import com.c332030.ctool4j.gradle.buildsrc.util.cExclusiveJdk8SourceDirs
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

/**
 * HTTP 客户端工具模块：`@UtilityClass` 的 `CHttpClientUtils`（全局复用的超时常量、连接池、
 * 长连接策略、`HttpClient` 与 Spring `ClientHttpRequestFactory`）。
 *
 * `src/main/java` 承 HttpClient 5（最新 LTS 档位）、`src/main/java-jdk8` 承 HttpClient 4
 * （jdk8 档位），两份同包同名源码按档位整体替换（追加会重复）。
 *
 * 独立成模块而不是留在 `ctool4j-core` 内，是为了让 `ctool4j-core` 的主源集保持单源：
 * HttpClient 4 与 5 的包名空间（`org.apache.http` vs `org.apache.hc.client5`）与 API 全然不同，
 * `HttpComponentsClientHttpRequestFactory`（Spring 7 / Boot 4）也只接受 HC5，整类不可调和；
 * 而该类是 core 里唯一被档位分流的类，拆出去后 core 的 `src/main/java` 不再有变体目录。
 * 使用 `CHttpClientUtils` 的模块显式引入本模块（如 `ctool4j-spring` 的 `CRestTemplateUtils`）。
 */
dependencies {

    // HttpClient：按档位取——jdk8 → `httpclient-jdk8`（HC4 4.5.14 末版），其余 → `httpclient`（HC5，Boot 4 BOM 管理）
    api(lib("httpclient"))

    // REQUEST_FACTORY / HTTP_CLIENT 等公共常量的对外类型含 spring-web 类型
    //（ClientHttpRequestFactory），spring-web 由根脚本对 component 档位注入的
    // compileOnly（spring-boot-starter-web）提供，与原 ctool4j-core 时期的取法一致

}

cExclusiveJdk8SourceDirs("java-jdk8")
cExclusiveJdk8SourceDirs("java-jdk8", "test")
