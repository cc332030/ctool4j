import com.c332030.ctool4j.gradle.buildsrc.util.cApiStrict

plugins {
    `java-library`
}

// 面向 mybatis-plus 3.3.x 使用方：版本钉住 3.3.2（版本本身即模块语义）
cApiStrict("mybatis-plus-boot-starter-33")

dependencies {

    api(project(":ctool4j-mybatis-base"))

}
