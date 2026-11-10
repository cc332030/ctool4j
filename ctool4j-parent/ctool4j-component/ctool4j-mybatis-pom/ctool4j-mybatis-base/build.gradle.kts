import com.c332030.ctool4j.gradle.buildsrc.util.cProvided
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    cProvided(lib("mybatis-plus-boot-starter"))

    api(project(":ctool4j-mybatis-adapter"))
    api(project(":ctool4j-transaction"))
    api(project(":ctool4j-db"))

    cProvided(project(":ctool4j-redis"))

    /** `CMpController` 的参数级必填用 `@CRequired`（javax/jakarta 两栈的统一抽象），仅编译期需要 */
    cProvided(project(":ctool4j-web"))

}

/**
 * 本模块**只有一个主源码目录** `src/main/java`：版本差异已由 `ctool4j-mybatis-adapter` 吸收
 * （它是全仓唯一允许出现多源的模块），容器（javax/jakarta）差异已由 ctool4j-web 的 `@CRequired`
 * 等中立类型吸收。
 *
 * 落点：
 * - `service/ICService` 一族（含 `ICCheckService`）、`service/impl/CServiceImpl`
 * - `controller/CMpController`（单份、对四个 MP 版本都成立，见类 javadoc）
 * - mapper/model/handler/injector/util 等
 * - `src/main/resources` 资源
 *
 * 本模块**不得**再出现 `java-javax` / `java-jakarta` / `java-jdk8` / `java-latest` 等第二个源目录，
 * 也不得 `srcDir` 指向别的模块。
 */
