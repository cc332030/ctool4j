plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    api(lib("janino"))

    api(project(":ctool4j-spring"))
    api(project(":ctool4j-spring-cloud"))
    api(project(":ctool4j-web"))

}
