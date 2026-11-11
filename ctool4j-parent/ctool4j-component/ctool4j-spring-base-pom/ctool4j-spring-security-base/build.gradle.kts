import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

/**
 * 安全侧抽象契约与容器适配模块：`CUnauthorizedHandler` / `CForbiddenHandler` 与默认实现、
 * `CSecurityResponseUtils`（`src/main/java`，零容器依赖），以及 Spring Security 定签名的容器落地
 * （`CAuthenticationEntryPoint` / `CAccessDeniedHandler` / `CSecurityResponseWriter`，jdk8 侧在
 * `src/main/java-jdk8`、最新 LTS 侧在 `src/main/java-latest`，追加形态、两目录永不共存）。
 * servlet-api 按档位取自版本目录 `servlet-api`（jdk8 → javax，最新 LTS → jakarta），声明为 optional。
 */
dependencies {

    api("org.springframework.boot:spring-boot-starter-security")
    cOptional(lib("servlet-api"))

    api(project(":ctool4j-spring-base"))
    api(project(":ctool4j-http"))
    api(project(":ctool4j-core"))
    api(project(":ctool4j-definition"))

}
