import com.c332030.ctool4j.gradle.buildsrc.util.*

plugins {
    `java-library`
}

// 面向 mybatis-plus 3.4.x 使用方：版本钉住 3.4.3.4（版本本身即模块语义）
cApiStrict("mybatis-plus-boot-starter-34")

dependencies {

    cOptional(lib("mybatis-plus-join"))

    api(project(":ctool4j-mybatis-base"))
    api(project(":ctool4j-mybatis-mp-java8"))

    // 共享的 service 源码（ICMpLockService 等）在本模块编译，需与 base 相同的 provided 依赖
    cProvided(project(":ctool4j-redis"))

}

/**
 * 挂载中间模块的 **3.3.x / 3.4.x 侧**自适应源码目录。
 *
 * 中间模块 `ctool4j-mybatis-mp-java8` 的公共面（`ICCheckService`、`CMybatisPlusSide`）随其 jar 发布；
 * 版本侧部分（自适应层 `spi/IMybatisPlusSpi` / `spi/IService` / `spi/CMybatisPlusServiceImpl`
 * 与实现基底 `service/impl/CBaseServiceImpl`）由本模块按侧纳入：
 * 本模块是 mybatis-plus 3.3.x / 3.4.x（`com.baomidou.mybatisplus.extension.service`）侧，
 * 故挂 `java-adaptive-extension`；3.5.x 侧的同名文件落 `java-adaptive-spring`、由 `ctool4j-mybatis` 挂载。
 *
 * 两侧的相对路径集合完全镜像、内容互不相交，任一模块只挂其一——于是本模块的源码集合里
 * 不会出现重复的全限定名，公共面也不引用任何版本侧符号。
 *
 * 挂载方式：`srcDir` 直接指向中间模块的源码目录（路径相对本模块项目目录解析）——
 * 目标包名由文件自身的 `package` 声明决定，与挂在哪个物理目录无关。
 */
sourceSets {
    main {
        java {
            srcDir("../ctool4j-mybatis-mp-java8/src/main/java-adaptive-extension")
            srcDir("../ctool4j-mybatis-mp-java8/src/main/java-base-extension")
        }
    }
}
