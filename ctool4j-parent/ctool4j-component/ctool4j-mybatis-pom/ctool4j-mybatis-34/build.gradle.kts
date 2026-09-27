import com.c332030.ctool4j.gradle.buildsrc.util.*

plugins {
    `java-library`
}

// 面向 mybatis-plus 3.4.x 使用方：版本钉住 3.4.3.4（版本本身即模块语义）
cApiStrict("mybatis-plus-boot-starter-34")

dependencies {

    cOptional(lib("mybatis-plus-join"))

    api(project(":ctool4j-mybatis-base"))

    // 共享的 service 源码（ICMpLockService 等）在本模块编译，需与 base 相同的 provided 依赖
    cProvided(project(":ctool4j-redis"))

}

/** 档位专属源码目录后缀：jdk8 档只读 jdk8，其余只读 latest（与根脚本约定一致） */
val jdkSideSuffix = if (isJdk8()) "jdk8" else "latest"

/**
 * 复用 base 里的"版本相关"源码，并挂载本版本侧的适配桥。
 *
 * 源码分三处（都只有唯一一份，由各版本模块按需纳入；base 自身不编译它们）：
 * - `java-mp`                     版本无关：ICService 一族、CBizIdUtils、CMybatisPlusSide
 * - `java-mp-bridge-ext`      本侧适配桥（`spi/IService` / `spi/IMybatisPlusSpi` /
 *                                 `spi/CMybatisPlusServiceImpl` / `service/ICCheckService` /
 *                                 `service/impl/CBaseServiceImpl`）
 * - `java-mp-{jdk8|latest}-ext`  档位专属控制器（`CMpController`）：同包同名两侧各一份，按档位二选一
 *
 * **两个桥目录内容互不相交**（本次改造的核心）：`java-mp-bridge`（3.5.x 侧）与
 * `java-mp-bridge-ext`（3.3.x/3.4.x 侧）承载完全相同的**相对路径集合**，
 * 但每份只承本侧的 MP 坐标（`spring.service` 或 `extension.service`），且**从不同时挂载**——
 * 于是任一模块的目录集合里都不会出现重复的全限定名，公共桶 `java-mp` 也不再引用任何版本侧符号，
 * 满足仓库「一个模块下的多个源目录之间代码必须完全互斥」的硬约束。
 *
 * 挂载方式：`srcDir` 直接指向 base 的源码目录（路径相对本模块项目目录解析）——
 * 目标包名由文件自身的 `package` 声明决定，与挂在哪个物理目录无关。
 */
sourceSets {
    main {
        java {
            srcDir("../ctool4j-mybatis-base/src/main/java-mp")
            srcDir("../ctool4j-mybatis-base/src/main/java-mp-bridge-ext")
            srcDir("../ctool4j-mybatis-base/src/main/java-mp-$jdkSideSuffix-ext")
        }
    }
}
