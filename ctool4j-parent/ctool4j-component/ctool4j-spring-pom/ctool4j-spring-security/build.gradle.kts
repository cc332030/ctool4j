import com.c332030.ctool4j.gradle.buildsrc.util.cOptional

plugins {
    `java-library`
}

/**
 * Spring Security 契约与装配模块：`CSecurityConfiguration`（SS 5.7 / 7 的 `antMatchers` 与
 * `requestMatchers` 差异由模块内按档位追加的 `java-jdk8` / `java-latest` 源目录承载）、
 * 放行路径契约、安全用户模型、认证服务抽象、安全上下文工具与 Jackson 支持。
 */

dependencies {

    api("org.springframework.boot:spring-boot-starter-security")
    // `CSecurityConfiguration` 带 `@RefreshScope`
    api("org.springframework.cloud:spring-cloud-context")
    cOptional("org.springframework.boot:spring-boot-starter-data-redis")
    cOptional("org.springframework.session:spring-session-data-redis")

    api(project(":ctool4j-web"))
    api(project(":ctool4j-spring-security-base"))

}
