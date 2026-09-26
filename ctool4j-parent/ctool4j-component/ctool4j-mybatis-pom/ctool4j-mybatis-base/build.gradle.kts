import com.c332030.ctool4j.gradle.buildsrc.util.cProvided
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    cProvided(lib("mybatis-plus-boot-starter"))

    api(project(":ctool4j-transaction"))
    api(project(":ctool4j-db"))

    cProvided(project(":ctool4j-redis"))

}

/**
 * 版本相关源码不放在默认源码目录，故本模块不编译它们。
 *
 * mybatis-plus 的 IService/ServiceImpl 在 3.3.x/3.4.x（`extension.service`）与 3.5.x（`spring.service`）
 * 中包名不同，一份字节码无法通吃，而下列源码正处在以 IService 为根的继承链上：
 * - `src/main/java-mp/**`           ICCheckService 及下游接口、CBaseServiceImpl、CBizIdUtils
 * - `src/main/java-mp-jdk8|latest`  CMpController（按档位二选一）
 *
 * 这些源码仍只有唯一一份（就在上面的目录里），由各版本模块按需 srcDir 纳入并各自编译；
 * base 自身不声明这些目录，故不会编译到它们。
 */
