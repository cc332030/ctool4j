plugins {
    `java-library`
}

import com.c332030.ctool4j.gradle.buildsrc.util.cOptional
import com.c332030.ctool4j.gradle.buildsrc.util.lib

dependencies {

    api(lib("commons-text"))
    cOptional(lib("httpmime"))
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
