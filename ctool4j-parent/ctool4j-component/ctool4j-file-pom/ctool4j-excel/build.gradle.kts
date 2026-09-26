plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    api(project(":ctool4j-core"))
    api(lib("easyexcel"))

}
