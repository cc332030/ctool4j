plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
/**
 * 容器侧适配模块：`ctool4j-spring-security-servlet` 内在 `src/main/java-javax` 与 `src/main/java-jakarta`
 * 两套同包同名源码间按档位自动切换（见根 `build.gradle.kts`），故依赖模块恒定、不再按侧拼接模块名。
 */

dependencies {

    api("org.springframework.boot:spring-boot-starter-security")
    cOptional("org.springframework.boot:spring-boot-starter-data-redis")
    cOptional("org.springframework.session:spring-session-data-redis")
    api("org.springframework.cloud:spring-cloud-context")

    api(project(":ctool4j-web"))
    api(project(":ctool4j-spring-security-servlet"))

}
