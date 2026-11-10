import com.c332030.ctool4j.gradle.buildsrc.util.cProvided
import com.c332030.ctool4j.gradle.buildsrc.util.isJdk8
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    /**
     * MP 只做**编译期可见**（`compileOnly` + 本模块测试可见），不向下游传递。
     *
     * 各使用方自己声明 MP 坐标：`ctool4j-mybatis-base` 用 `cProvided`、`ctool4j-mybatis-33` / `-34`
     * 用 `cApiStrict` 钉住 3.3 / 3.4、`ctool4j-mybatis` 用 `api`。若本模块用 `api` 传递 MP，
     * 3.3.2 / 3.4.3.4 的 `strictly` 会与传递来的 3.5.x 要求冲突、直接解析失败。
     */
    cProvided(lib("mybatis-plus-boot-starter"))

    /** 自适应层（`spi/CMybatisPlusSpi`）的空安全实现用到 `CList`；hutool 的 `CollUtil` 由 core 传递 */
    api(project(":ctool4j-core"))

}

/**
 * 版本适配层（**全仓唯一允许出现多源的模块**）。
 *
 * 两个源目录承载**同一组全限定名**（`com.c332030.ctool4j.mybatisplus.spi` 下的 `IService`、
 * `CMybatisPlusSpi`、`CMybatisPlusServiceImpl`），差异只在"承哪个 MP 包名"：
 *
 * - `src/main/java`      MP >= 3.5.17：`com.baomidou.mybatisplus.spring.service`（**默认档位**）
 * - `src/main/java-jdk8` MP < 3.5.17：`com.baomidou.mybatisplus.extension.service`（jdk8 档位）
 *
 * 「当前/默认」侧用标准目录 `src/main/java`，「旧档」侧挂 `java-jdk8`——与 Maven 的"主源码目录 +
 * profile 专属目录"习惯一致，IDE 里不再出现 `java-latest` 这类会过期的名字。
 *
 * 与根脚本档位机制的关系：根脚本对 `java-<side>` 是**追加**（jdk8 档位在有 `java-jdk8` 时仍会编
 * `src/main/java`）。本模块两目录同名同包，追加必然重复，故 jdk8 档位在此把主源集**整体替换**为
 * `java-jdk8`。这是全仓唯一需要"替换"而非"追加"的模块（其余模块的多源目录是"公共 + 档位增量"，
 * 追加语义正好）。两个目录**永不共存**：同包同名的两份 class 若同时进 classpath，JVM 取先命中者
 * 且不报错，属于最难排查的一类问题。
 *
 * 使用方（`ctool4j-mybatis-base` 与各版本模块）消费的是**按档位编好的 jar**，
 * 不再从别的模块 `srcDir` 挂载源码——版本差异到此为止。
 */
if (isJdk8()) {

    /**
     * jdk8 档位：主源集只留 `java-jdk8`（`src/main/java` 是 3.5.17+ 侧，与 jdk8 档的 MP 3.5.16
     * 不是同一侧）。
     */
    sourceSets.named("main") {
        java.setSrcDirs(listOf("src/main/java-jdk8"))
    }

}
