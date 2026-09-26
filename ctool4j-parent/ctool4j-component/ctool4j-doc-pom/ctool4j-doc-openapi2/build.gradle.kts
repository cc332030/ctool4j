plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    api(lib("knife4j-openapi2-spring-boot-starter"))

    api(project(":ctool4j-web"))

}
