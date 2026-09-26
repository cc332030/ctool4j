plugins {
    `java-platform`
}

import com.c332030.ctool4j.gradle.buildsrc.util.lib
import org.gradle.api.plugins.JavaPlatformExtension

/**
 * <p>
 * Description: ctool4j-bom 构建脚本
 * </p>
 *
 * BOM：统一管理第三方依赖版本（对应迁移前同名 pom 的 `<dependencyManagement>`），
 * 并 import 三个上游 BOM 与自研构件 BOM（`ctool4j-dependencies`）。
 */

extensions.configure<JavaPlatformExtension> {
    allowDependencies()
}

dependencies {

    // ---- 上游 BOM：以 import 形式发布 ----
    api(platform(lib("spring-boot-dependencies")))
    api(platform(lib("spring-cloud-dependencies")))
    api(platform(lib("spring-cloud-alibaba-dependencies")))
    // ---- 自研构件版本 ----
    api(platform(project(":ctool4j-dependencies")))

    constraints {

        // ---- 迁移前显式钉住的第三方版本（两个 JDK 档位各自取值）----
        api(lib("mybatis-plus-boot-starter"))
        api(lib("mybatis-plus-jsqlparser-4-9"))
        api(lib("dynamic-datasource-spring-boot-starter"))
        api(lib("mybatis-plus-join"))
        api(lib("redisson"))
        api(lib("rocketmq"))
        api(lib("jetcache"))
        api(lib("knife4j-openapi2-spring-boot-starter"))
        api(lib("hutool"))
        api(lib("guava"))
        api(lib("commons-text"))
        api(lib("commons-io"))
        api(lib("commons-csv"))
        api(lib("java-uuid-generator"))
        api(lib("jnanoid"))
        api(lib("ulid-creator"))
        api(lib("mapstruct"))
        api(lib("transmittable-thread-local"))
        api(lib("xxl-job"))
        api(lib("easyexcel"))
        api(lib("minio"))

    }

}
