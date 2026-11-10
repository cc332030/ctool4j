import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

plugins {
    `java-library`
}

dependencies {

    // HttpClient：按档位取——jdk8 → `httpclient-jdk8`（HC4 4.5.14 末版），其余 → `httpclient`（HC5，Boot 4 BOM 管理）；
    // 供 `CHttpClientUtils`（java-jdk8 / java-latest 各一份同包同名实现）使用
    api(lib("httpclient"))

    api(lib("commons-text"))
    api(lib("guava"))
    cOptional(lib("java-uuid-generator"))
    cOptional(lib("jnanoid"))
    cOptional(lib("ulid-creator"))
    api(lib("slf4j-api"))
    api(lib("transmittable-thread-local"))
    api(lib("caffeine"))

    api(project(":ctool4j-definition"))

    testImplementation(project(":ctool4j-test-definition"))
    testImplementation(lib("cglib"))

}
