plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    cOptional(lib("javax-servlet-api"))

    api(project(":ctool4j-http-base"))

}
