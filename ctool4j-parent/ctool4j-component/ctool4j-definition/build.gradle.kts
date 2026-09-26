plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cProvided
import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    cProvided(lib("mybatis-plus-boot-starter"))

    api(lib("hutool"))

}
