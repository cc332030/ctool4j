import com.c332030.ctool4j.gradle.buildsrc.util.isJdk8

plugins {
    `java-library`
}

/**
 * 容器侧适配已并回 `ctool4j-spring-base`：容器契约类落在其 `src/main/java-jdk8`（javax）
 * 与 `src/main/java-latest`（jakarta）档位源目录内，追加语义天然互斥（见根 `build.gradle.kts`），
 * 故本模块依赖恒定为 `ctool4j-spring-base`、不再按侧拼接模块名。
 */

dependencies {

    // HttpClient 4：仅 jdk8 档位需要（Boot 2.7 的 Spring 5 HttpComponentsClientHttpRequestFactory 走 HC4）；
    // 最新 LTS 档位的 Spring 7 只接受 HttpClient 5，由根脚本按档位注入 httpclient5
    if (isJdk8()) {
        testImplementation("org.apache.httpcomponents:httpclient")
    }

    api("org.aspectj:aspectjweaver")
    api(project(":ctool4j-core"))
    api(project(":ctool4j-spring-base"))
    api(project(":ctool4j-autowired-processor"))

}
