plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.isJdk8

/**
 * 容器侧适配模块：`ctool4j-spring-servlet` 内在 `src/main/java-javax` 与 `src/main/java-jakarta`
 * 两套同包同名源码间按档位自动切换（见根 `build.gradle.kts`），故依赖模块恒定、不再按侧拼接模块名。
 */

dependencies {

    // HttpClient 4：仅 jdk8 档位需要（Boot 2.7 的 Spring 5 HttpComponentsClientHttpRequestFactory 走 HC4）；
    // 最新 LTS 档位的 Spring 7 只接受 HttpClient 5，由根脚本按档位注入 httpclient5
    if (isJdk8()) {
        testImplementation("org.apache.httpcomponents:httpclient")
    }

    api("org.aspectj:aspectjweaver")
    api(project(":ctool4j-core"))
    api(project(":ctool4j-spring-servlet"))
    api(project(":ctool4j-autowired-processor"))

}
