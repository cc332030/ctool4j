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
 * 复用 base 里唯一一份"版本相关"源码，并挂载本版本侧的适配桥。
 *
 * 源码分四处（都只有唯一一份，由各版本模块按需纳入；base 自身不编译它们）：
 * - `java-mp`                    版本无关：ICCheckService 及下游接口、CBaseServiceImpl、CBizIdUtils
 * - `java-mp-bridge`             3.5.x 侧的适配桥（`mybatisplus/spi/IService`、`ServiceImpl`），
 *                                承 `spring.service` 坐标；另含两侧共用的符号占位 `CMybatisPlusSide`
 * - `java-mp-bridge-ext`         3.3.x/3.4.x 侧的适配桥（同样是 `mybatisplus/spi/IService`、
 *                                `ServiceImpl`，承 `extension.service` 坐标）；两个版本侧共用这一份
 * - `java-mp-{jdk8|latest}-ext`  档位专属控制器（`CMpController` 等）：同包同名两侧各一份，按档位二选一
 *
 * 「同全限定名两份」不可避免（mybatis-plus 3.5.17 起把 `IService`/`ServiceImpl` 由
 * `extension.service` 迁到 `spring.service`，两坐标不能共存），解法是让两份**落在互不相交的目录**里、
 * 由各版本模块按需挂载其一。因此任一模块的源码目录集合里都不会出现重复的全限定名，满足仓库
 * 「一个模块下的多档位源目录必须完全互斥」的硬约束。
 *
 * 挂载方式：`srcDir` 直接指向 base 的源码目录（路径相对本模块项目目录解析）——
 * 目标包名由文件自身的 `package` 声明决定，与挂在哪个物理目录无关。
 */
sourceSets {
    main {
        java {
            srcDir("../ctool4j-mybatis-base/src/main/java-mp")
            srcDir("../ctool4j-mybatis-base/src/main/java-mp-bridge")
            srcDir("../ctool4j-mybatis-base/src/main/java-mp-$jdkSideSuffix-ext")
        }
    }
}
