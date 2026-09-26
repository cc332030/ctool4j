plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.isJdk8

/**
 * 容器侧切换点：jdk8 档位取 javax 侧适配模块，其他档位取 jakarta 侧。
 *
 * 业务代码与 import 不变——换的是依赖模块（见 `agent/AGENTS-PROJECT.MD`「javax/jakarta 双栈」）。
 * 另一侧模块不在本档位的构建里（见 `settings.gradle.kts` 的档位过滤）。
 */
val containerSide = if (isJdk8()) "javax" else "jakarta"

dependencies {

    // HttpClient 4：仅 jdk8 档位需要（Boot 2.7 的 Spring 5 HttpComponentsClientHttpRequestFactory 走 HC4）；
    // 最新 LTS 档位的 Spring 7 只接受 HttpClient 5，由根脚本按档位注入 httpclient5
    if (isJdk8()) {
        testImplementation("org.apache.httpcomponents:httpclient")
    }

    api("org.aspectj:aspectjweaver")
    api(project(":ctool4j-core"))
    api(project(":ctool4j-spring-$containerSide"))
    api(project(":ctool4j-autowired-processor"))

}
