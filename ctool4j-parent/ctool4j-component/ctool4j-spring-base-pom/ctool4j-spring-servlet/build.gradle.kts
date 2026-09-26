plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

/**
 * Spring 侧 Servlet 适配模块：承载 Spring 扩展点签名里带 Servlet 类型的容器落地类与同包同名桥接接口。
 *
 * 容器侧（javax / jakarta）由根脚本按 JDK 档位选用源码目录 `src/main/java-javax` 或 `src/main/java-jakarta`
 * （两目录同包同名，只有 Servlet import 不同），故**不再拆成两个模块**。
 * servlet-api 按档位取自版本目录 `servlet-api`（jdk8 → javax，最新 LTS → jakarta），声明为 optional。
 */
dependencies {

    cOptional(lib("servlet-api"))

    api(project(":ctool4j-http-servlet"))
    api(project(":ctool4j-spring-base"))
    api(project(":ctool4j-core"))

}
