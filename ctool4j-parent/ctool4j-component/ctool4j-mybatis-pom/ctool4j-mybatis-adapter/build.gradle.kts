import com.c332030.ctool4j.gradle.buildsrc.util.cExclusiveJdk8SourceDirs
import com.c332030.ctool4j.gradle.buildsrc.util.cProvided
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
 * 版本适配层：`src/main/java` 放「默认侧」，另一侧挂档位目录、**整体替换**。
 *
 * 两个源目录承载**同一组全限定名**（`com.c332030.ctool4j.mybatisplus.spi` 下的 `IService`、
 * `CMybatisPlusSpi`、`CMybatisPlusServiceImpl`），差异只在"承哪个 MP 包名"：
 *
 * - `src/main/java`      MP >= 3.5.17：`com.baomidou.mybatisplus.spring.service`（**默认档位**）
 * - `src/main/java-jdk8` MP < 3.5.17：`com.baomidou.mybatisplus.extension.service`（jdk8 档位）
 *
 * 两目录同名同包，根脚本的"追加"语义会重复（同包同名的两份 class 同时进 classpath 时 JVM
 * 取先命中者、**不报错**，属最难排查的一类问题），故用 `cExclusiveJdk8SourceDirs` 整体替换。
 *
 * 使用方（`ctool4j-mybatis-base` 与各版本模块）消费的是**按档位编好的 jar**，
 * 不再从别的模块 `srcDir` 挂载源码——版本差异到此为止。
 */
cExclusiveJdk8SourceDirs("java-jdk8")
