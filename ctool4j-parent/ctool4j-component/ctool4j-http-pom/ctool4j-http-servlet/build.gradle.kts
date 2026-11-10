import com.c332030.ctool4j.gradle.buildsrc.util.cExclusiveJdk8SourceDirs
import com.c332030.ctool4j.gradle.buildsrc.util.cOptional

plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cExclusiveJdk8SourceDirs
import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

/**
 * Servlet 侧适配模块：承载 `ctool4j-http-base` 契约的容器落地。
 *
 * 容器侧（javax / jakarta）同包同名、只有 Servlet import 不同，故**不再拆成两个模块**：
 * `src/main/java` 是 jakarta（默认/最新 LTS）侧，`src/main/java-javax` 是 jdk8 侧、整体替换（见下）。
 * servlet-api 按档位取自版本目录 `servlet-api`（jdk8 → javax，最新 LTS → jakarta），声明为 optional。
 */
dependencies {

    cOptional(lib("servlet-api"))

    api(project(":ctool4j-http-base"))

}

/** jdk8 档位用 `java-javax` 整体替换主源集；两目录同包同名，追加会重复 */
cExclusiveJdk8SourceDirs("java-javax")
