import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

/**
 * Spring 侧抽象契约与容器适配模块：`ICHandlerInterceptor` / `ICResponseBodyAdvice`、`@CConfigurationProperties`
 * 与 cors 配置/工具等契约（`src/main/java`，零容器依赖），以及契约的容器落地
 * （`CRequestUtils` / `CErrorController` / `CAbstractWebAuthFilter` 等，jdk8 侧在 `src/main/java-jdk8`、
 * 最新 LTS 侧在 `src/main/java-latest`，追加形态、两目录永不共存）。
 * servlet-api 按档位取自版本目录 `servlet-api`（jdk8 → javax，最新 LTS → jakarta），声明为 optional。
 */
dependencies {

    api("org.springframework.boot:spring-boot")
    cOptional(lib("servlet-api"))

    api(project(":ctool4j-http"))
    api(project(":ctool4j-core"))
    api(project(":ctool4j-autowired-processor"))

}
