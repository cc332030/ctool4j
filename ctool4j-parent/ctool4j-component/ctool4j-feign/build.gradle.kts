plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cOptional

dependencies {

    api("org.springframework.cloud:spring-cloud-starter-openfeign")
    api("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    cOptional("io.github.openfeign:feign-httpclient")

    api(project(":ctool4j-spring-cloud"))
    api(project(":ctool4j-log-base"))

}
