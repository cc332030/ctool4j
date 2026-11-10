import com.c332030.ctool4j.gradle.buildsrc.util.cProvided
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    /**
     * MP 只做**编译期可见**，不向下游传递。
     *
     * 各使用方自己声明 MP 坐标：本模块的 `spi` 适配层与契约编译用 `cProvided`、
     * `ctool4j-mybatis-33` / `-34` 用 `cApiStrict` 钉住 3.3 / 3.4、`ctool4j-mybatis` 用 `api`。
     * 若用 `api` 传递 MP，3.3.2 / 3.4.3.4 的 `strictly` 会与传递来的 3.5.x 要求冲突、直接解析失败。
     */
    cProvided(lib("mybatis-plus-boot-starter"))

    // `spi` 适配层的空安全实现用到 `CList`；hutool 的 `CollUtil` 由 core 传递
    api(project(":ctool4j-core"))
    api(project(":ctool4j-transaction"))
    api(project(":ctool4j-db"))

    cProvided(project(":ctool4j-redis"))

    /** `CMpController` 的参数级必填用 `@CRequired`（javax/jakarta 两栈的统一抽象），仅编译期需要 */
    cProvided(project(":ctool4j-web"))

}

/**
 * 落点：
 * - 契约与增强基座：`service/ICService` 一族（含 `ICCheckService`）、`service/impl/CServiceImpl`
 * - `controller/CMpController`（单份、对四个 MP 版本都成立，见类 javadoc）
 * - mapper/model/handler/injector/util 等
 * - **版本适配层 `spi`**（原 `ctool4j-mybatis-adapter`，已并回）：`IService` / `CMybatisPlusSpi` /
 *   `CMybatisPlusServiceImpl` 用同一组中性全限定名承接 MP 包名迁移
 *   （3.5.17 起 `extension.service` → `spring.service`，两坐标不能共存）——
 *   `src/main/java-latest` 承 `spring.service`（MP >= 3.5.17，最新 LTS 档）、
 *   `src/main/java-jdk8` 承 `extension.service`（MP < 3.5.17，jdk8 档），
 *   两目录同包同名、永不共存，追加语义天然互斥；`src/main/java` 承公共契约
 * - `src/main/resources` 资源
 */
