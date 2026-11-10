import com.c332030.ctool4j.gradle.buildsrc.util.cApiStrict
import com.c332030.ctool4j.gradle.buildsrc.util.cProvided

plugins {
    `java-library`
}

// 面向 mybatis-plus 3.3.x 使用方：版本钉住 3.3.2（版本本身即模块语义）
cApiStrict("mybatis-plus-boot-starter-33")

dependencies {

    // 版本差异（`IService` / `ServiceImpl` 的包名）由 `ctool4j-mybatis-adapter` 吸收；
    // 适配模块的 jdk8 档产物即 extension 包名侧，与 3.3.x / 3.4.x / 3.5.16 同侧，
    // 经 base 的 `api` 传递到本模块，故这里不再单独声明。
    api(project(":ctool4j-mybatis-base"))

    // 共享的 service 源码（ICMpLockService 等）在本模块编译，需与 base 相同的 provided 依赖
    cProvided(project(":ctool4j-redis"))

}

/**
 * 本模块只在 **jdk8 档位**参与构建（`settings.gradle.kts` 的 `jdk8OnlyModuleNames`）。
 *
 * 原因：适配模块按档位出 jar——jdk8 档是 `extension.service` 包名侧（与 3.3.x 同侧），
 * 最新 LTS 档是 `spring.service` 包名侧（只对 3.5.17+ 成立）。3.3.x / 3.4.x 没有 spring 包名，
 * 与最新 LTS 档的适配 jar 不同侧，无从共存，故该档位不纳入本模块。
 */
