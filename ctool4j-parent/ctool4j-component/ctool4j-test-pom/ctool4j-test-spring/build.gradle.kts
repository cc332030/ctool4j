import com.c332030.ctool4j.gradle.buildsrc.util.cProvided

plugins {
    `java-library`
}

dependencies {

    cProvided("org.springframework.boot:spring-boot-starter-test")

    api(project(":ctool4j-spring"))
    api(project(":ctool4j-test-core"))

}
