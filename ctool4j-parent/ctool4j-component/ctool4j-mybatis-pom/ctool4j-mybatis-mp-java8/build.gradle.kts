import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    /**
     * 自适应层（`spi/IMybatisPlusSpi`）直接继承 MP 上游的 `IService`，故编译期必须可见 MP 坐标。
     *
     * 用 `api` 而非 `cProvided`：本模块的公共面与侧目录都不向下游重复声明 MP，
     * 消费模块（`ctool4j-mybatis*`）据此拿到 MP 抽象——base 对 MP 是 `compileOnly`（不传递），
     * 只有经本模块的 `api` 才看得到。默认侧取 3.5.x（`mybatis-plus-boot-starter`，
     * 与本模块编译期挂载的 `java-adaptive-spring` 一致）；3.3.x / 3.4.x 侧消费模块自带 pinned 坐标。
     */
    api(lib("mybatis-plus-boot-starter"))

    /** 自适应层的空安全实现用到 `CList`（ctool4j-core）与 `CollUtil`（hutool，经 ctool4j-definition 传递） */
    api(project(":ctool4j-core"))

}

/**
 * 版本侧源码目录：**默认侧随本模块一起编译，另一侧由消费模块 `srcDir` 覆盖**。
 *
 * 本模块承载"自适应中间层"（见 README）：
 * - `src/main/java`            版本无关公共面：`service/ICCheckService`、`CMybatisPlusSide`，随 jar 发布
 * - `src/main/java-adaptive-spring`  3.5.x 侧自适应层（`spi/` 下三个接口，**默认侧**，本模块编译）
 * - `src/main/java-adaptive-extension` 3.3.x / 3.4.x 侧自适应层（**覆盖侧**，不参与本模块编译）
 * - `src/main/java-base-{spring,extension}` 实现基底 `service/impl/CBaseServiceImpl`（**两个侧都由消费模块挂载**，
 *   本模块不编译：它交叉引用 base 的 `ICService` 与 `Mapper`，本模块不得反向依赖 base）
 *
 * 两个侧目录的相对路径集合互为镜像、内容互不相交。默认侧（3.5.x）随 jar 发布，
 * 故 `ctool4j-mybatis`（同为 3.5.x 侧）直接依赖 jar 即可；
 * `ctool4j-mybatis-33` / `-34`（3.3.x / 3.4.x 侧）在自己的源码集里 `srcDir` 挂 `java-adaptive-extension`，
 * **源码优先于 classpath 上的 jar**，同名类由此被换成 extension 侧实现——
 * 这正是"自适应"的落地机制，与仓库里 `java-javax` ↔ `java-jakarta` 按档位二选一同构。
 */
sourceSets.named("main") {
    java.srcDir("src/main/java-adaptive-spring")
}
