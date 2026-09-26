import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.cProvided
import com.c332030.ctool4j.gradle.buildsrc.util.isJdk8
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    api(lib("mybatis-plus-boot-starter"))
    api(lib("mybatis-plus-jsqlparser-4-9"))
    cOptional(lib("mybatis-plus-join"))

    api(project(":ctool4j-mybatis-base"))

    // 共享的 service 源码（ICMpLockService 等）在本模块编译，需与 base 相同的 provided 依赖
    cProvided(project(":ctool4j-redis"))

}

/** 档位专属源码目录后缀：jdk8 档只读 jdk8，其余只读 latest（与根脚本约定一致） */
val jdkSideSuffix = if (isJdk8()) "jdk8" else "latest"

/**
 * 复用 base 里唯一一份"版本相关"源码，叠加 3.5.x 侧的适配桥。
 *
 * 这些源码在 base 的独立目录里（base 自身不编译），各版本模块按需纳入：
 * - java-mp           ICCheckService 及下游接口、CBaseServiceImpl、CBizIdUtils
 * - java-mp-<档位>    CMpController（按档位二选一）
 * - java-mp-spring    3.5.x 侧适配桥（中性包名 spi.IService / spi.ServiceImpl）
 */
val mybatisBaseSrc = "../ctool4j-mybatis-base/src/main"

sourceSets {
    main {
        java {
            srcDir("$mybatisBaseSrc/java-mp")
            srcDir("$mybatisBaseSrc/java-mp-$jdkSideSuffix")
            srcDir("$mybatisBaseSrc/java-mp-spring")
        }
    }
}
