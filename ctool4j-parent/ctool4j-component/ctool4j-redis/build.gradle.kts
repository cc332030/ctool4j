plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    api("org.springframework.boot:spring-boot-starter-data-redis")
    api(lib("redisson"))

    api(project(":ctool4j-spring"))

}
