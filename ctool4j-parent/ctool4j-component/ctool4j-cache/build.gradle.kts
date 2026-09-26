plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    cOptional("org.springframework.boot:spring-boot-starter-data-redis")
    api(lib("jetcache"))

    api(project(":ctool4j-redis"))

}
