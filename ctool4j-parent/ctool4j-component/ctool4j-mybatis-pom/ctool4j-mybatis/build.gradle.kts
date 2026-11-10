import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.cProvided
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    // MP 版本按档位取：jdk8 档 3.5.16、最新 LTS 档 3.5.17（见 gradle/libs.versions.toml）
    api(lib("mybatis-plus-boot-starter"))
    api(lib("mybatis-plus-jsqlparser-4-9"))
    cOptional(lib("mybatis-plus-join"))

    // 适配层与 base 的版本差异已收敛在 `ctool4j-mybatis-adapter`（按档位出 jar），本模块只依赖 base
    api(project(":ctool4j-mybatis-base"))

    // 共享的 service 源码（ICMpLockService 等）在本模块编译，需与 base 相同的 provided 依赖
    cProvided(project(":ctool4j-redis"))

}

/**
 * 本模块是"当前主流版本"的聚合：MP 3.5.x（jdk8 档 3.5.16、最新 LTS 档 3.5.17）+ 本仓库的
 * injector / 配置增强。`IService` / `ServiceImpl` 的包名差异由 `ctool4j-mybatis-adapter`
 * 按档位二选一吸收，本模块的单源目录对两档都成立。
 */
