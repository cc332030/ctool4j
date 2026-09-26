plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    cOptional(lib("jakarta-servlet-api"))

    api(project(":ctool4j-core"))
    api(project(":ctool4j-http-jakarta"))
    api(project(":ctool4j-spring-base"))
    api(project(":ctool4j-spring-security-base"))

}
