plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.isJdk8

/** 容器侧切换点：jdk8 档位取 javax 侧适配模块，其他档位取 jakarta 侧 */
val containerSide = if (isJdk8()) "javax" else "jakarta"

dependencies {

    api("org.springframework.boot:spring-boot-starter-security")
    cOptional("org.springframework.boot:spring-boot-starter-data-redis")
    cOptional("org.springframework.session:spring-session-data-redis")
    api("org.springframework.cloud:spring-cloud-context")

    api(project(":ctool4j-web"))
    api(project(":ctool4j-spring-security-$containerSide"))

}
