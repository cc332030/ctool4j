import com.c332030.ctool4j.gradle.buildsrc.util.cExclusiveJdk8SourceDirs

plugins {
    `java-library`
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-logging")

    api(project(":ctool4j-log-base"))

}

/**
 * `CMdcLogback` 在 SLF4J 1.7（jdk8 档）/ 2.0（最新 LTS 档）下是**同一个全限定名的两份实现**，
 * 追加会重复，故 jdk8 档位用 `java-jdk8` 整体替换主源集。
 */
cExclusiveJdk8SourceDirs("java-jdk8")
