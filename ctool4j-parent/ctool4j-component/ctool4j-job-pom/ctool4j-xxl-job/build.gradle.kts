plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    api(lib("xxl-job"))

    api(project(":ctool4j-job-base"))

}
