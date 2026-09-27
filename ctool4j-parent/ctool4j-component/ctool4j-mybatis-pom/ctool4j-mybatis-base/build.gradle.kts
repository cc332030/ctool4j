import com.c332030.ctool4j.gradle.buildsrc.util.cProvided
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    cProvided(lib("mybatis-plus-boot-starter"))

    api(project(":ctool4j-mybatis-mp-java8"))
    api(project(":ctool4j-transaction"))
    api(project(":ctool4j-db"))

    cProvided(project(":ctool4j-redis"))

}

/**
 * 本模块的源码分家（**各目录之间必须完全互斥**）。
 *
 * - `src/main/java`              版本无关公共面：`ICService` 一族（`ICCheckService` 在中间模块）、
 *                                `CBizIdUtils`、mapper/model/handler/injector/util 等
 * - `src/main/java-javax`        jdk8 档位的容器侧适配（`CMpController`，`javax.validation`）
 * - `src/main/java-jakarta`      最新 LTS 档位的容器侧适配（`CMpController`，`jakarta.validation`）
 * - `src/main/resources`         资源
 *
 * `java-javax` ↔ `java-jakarta` 是**成对镜像**（同包同名、按 JDK 档位二选一），
 * 由根脚本的档位源码目录机制整体放行——与 `ctool4j-http-servlet` 等同构；
 * 两目录互为镜像、各承一侧容器类型，一致性由「成对文件互引 + 归一化比对」维护。
 *
 * 版本侧差异（mybatis-plus 3.5.17 起 `IService`/`ServiceImpl` 由 `extension.service` 迁到
 * `spring.service`）**不在本模块**：已下沉到中间模块 `ctool4j-mybatis-mp-java8`，
 * 由自适应层（`spi/IMybatisPlusSpi` 两侧各一份）吸收，公共契约 `ICService` 一族因此版本无关。
 */
