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
 * - `src/main/java-mp/**`              版本无关：ICService 一族、CBizIdUtils、CMybatisPlusSide
 * - `src/main/java-mp-bridge/**`       3.5.x 侧适配桥（`spi/IService` / `spi/IMybatisPlusSpi` /
 *                                      `spi/CMybatisPlusServiceImpl` / `service/ICCheckService` /
 *                                      `service/impl/CBaseServiceImpl`）
 * - `src/main/java-mp-bridge-ext/**`   3.3.x/3.4.x 侧适配桥（相对路径集合与 java-mp-bridge 完全镜像）
 * - `src/main/java-mp-{jdk8|latest}-ext/**`  档位专属控制器（CMpController，按档位二选一）
 *
 * 这些源码仍只有唯一一份（就在上面的目录里），由各版本模块按需 srcDir 纳入并各自编译；
 * base 自身不声明这些目录，故不会编译到它们。
 *
 * **目录之间完全互斥**（本次改造的核心）：`java-mp` 只放版本无关的公共面，
 * 两个 `java-mp-bridge*` 目录承载同一全限定名的两侧实现且**内容互不相交**、任一版本模块只挂其一，
 * 档位专属控制器两侧同名、由档位二选一 —— 于是"任一模块的目录集合里都不会出现重复的全限定名"，
 * 也不存在"公共桶里的源码依赖只在某一版本侧存在的符号"这种隐式耦合。
 *
 * 目录后缀统一为 `-ext`，正是为了让这几个目录**不被**根脚本认作档位专属源目录
 * （根脚本只认 `src/<main|test>/java-jdk8`、`java-latest`、`java-mp-jdk8`、`java-mp-latest`）——
 * 这些目录的最终形态是"挂在 base 名下、由版本模块编译"，参不参与 base 自身的源目录是另一回事。
 */
