plugins {
    `java-library`
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-logging")

    api(project(":ctool4j-log-base"))

}

/**
 * `CMdcLogback` 在 SLF4J 1.7（jdk8 档）/ 2.0（最新 LTS 档）下是**同一个全限定名的两份实现**：
 * 1.7 侧落在 `src/main/java-jdk8`，2.0 侧（即资源与通用代码之外的核心类）落在 `src/main/java-latest`，
 * 两个目录互斥、按档位各取其一，追加语义天然不重复。
 */
