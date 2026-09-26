import com.c332030.ctool4j.gradle.buildsrc.util.cApiStrict
import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

// 面向 mybatis-plus 3.4.x 使用方：版本钉住 3.4.3.4（版本本身即模块语义）
cApiStrict("mybatis-plus-boot-starter-34")

dependencies {

    cOptional(lib("mybatis-plus-join"))

    api(project(":ctool4j-mybatis-base"))

}
