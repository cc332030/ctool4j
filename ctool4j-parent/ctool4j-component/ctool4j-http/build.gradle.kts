import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

/**
 * Servlet 抽象契约与容器适配模块：`CHttpRequest` / `CHttpResponse` / `CFilter` 等契约，
 * 以及契约的容器落地（`CHttpServletRequest` 等）。
 *
 * 契约类（零 Servlet 依赖）在 `src/main/java`；容器侧（javax / jakarta）同包同名、只有 Servlet import 不同，
 * jdk8 侧放 `src/main/java-jdk8`、最新 LTS 侧放 `src/main/java-latest`（追加形态，两目录永不共存）。
 * servlet-api 按档位取自版本目录 `servlet-api`（jdk8 → javax，最新 LTS → jakarta），声明为 optional。
 */
dependencies {

    cOptional(lib("servlet-api"))

}
