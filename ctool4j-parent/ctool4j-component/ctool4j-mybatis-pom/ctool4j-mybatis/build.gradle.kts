import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    // MP 版本按档位取：jdk8 档 3.5.16、最新 LTS 档 3.5.17（见 gradle/libs.versions.toml）
    api(lib("mybatis-plus-boot-starter"))
    api(lib("mybatis-plus-jsqlparser-4-9"))
    cOptional(lib("mybatis-plus-join"))

    // 版本差异已收敛在 base 的 `spi` 适配层（java-latest / java-jdk8 追加承载），本模块只依赖 base
    api(project(":ctool4j-mybatis-base"))

    // 本模块源码只做版本差异（插件 / 注入器签名），共享的 service 契约（含 ICMpLockService）
    // 都在 base，其 provided 依赖由 base 自己声明，此处不再重复。

}

/**
 * 本模块是"当前主流版本"的聚合：MP 3.5.x（jdk8 档 3.5.16、最新 LTS 档 3.5.17）+ 本仓库的
 * injector / 配置增强。`IService` / `ServiceImpl` 的包名差异由 base 的 `spi` 适配层
 * 按档位二选一吸收，本模块的单源目录对两档都成立。
 */
