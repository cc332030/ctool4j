plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    api(lib("mybatis-plus-boot-starter"))
    api(lib("mybatis-plus-jsqlparser-4-9"))
    cOptional(lib("mybatis-plus-join"))

    api(project(":ctool4j-mybatis-base"))

}
