plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cOptional

dependencies {

    cOptional("org.springframework.boot:spring-boot-starter-jdbc")

    api(project(":ctool4j-spring"))

}
