plugins {
    `java-platform`
}

import org.gradle.api.plugins.JavaPlatformExtension

/**
 * <p>
 * Description: ctool4j-dependencies 构建脚本
 * </p>
 *
 * BOM：统一管理 ctool4j 自研构件的版本（对应迁移前同名 pom 的 `<dependencyManagement>`）。
 *
 * 只约束"本次 JDK 档位实际纳入构建"的模块——另一侧容器模块不在本档位发布，
 * 故不给它们留下指向不存在构件的版本约束。
 */

extensions.configure<JavaPlatformExtension> {
    allowDependencies()
}

/** 自研构件清单（与迁移前 `ctool4j-dependencies` 的登记表一致，顺序保持一致） */
val ctool4jArtifacts = listOf(
    "ctool4j-processor-base",
    "ctool4j-autowired-processor",
    "ctool4j-mq-processor",
    "ctool4j-mybatis-processor",

    "ctool4j-definition",
    "ctool4j-core",
    "ctool4j-log-base",
    "ctool4j-auth-pom",
    "ctool4j-auth-base",
    "ctool4j-auth-spring",
    "ctool4j-logback",
    "ctool4j-log4j",
    "ctool4j-http-base",
    "ctool4j-http-javax",
    "ctool4j-http-jakarta",
    "ctool4j-spring-base",
    "ctool4j-spring-javax",
    "ctool4j-spring-jakarta",
    "ctool4j-spring-security-base",
    "ctool4j-spring-security-javax",
    "ctool4j-spring-security-jakarta",
    "ctool4j-mq-base",
    "ctool4j-rocketmq",
    "ctool4j-rabbitmq",
    "ctool4j-spring",
    "ctool4j-spring-security",
    "ctool4j-spring-cloud",
    "ctool4j-web",
    "ctool4j-redis",
    "ctool4j-cache",
    "ctool4j-nacos-config",
    "ctool4j-nacos-discovery",
    "ctool4j-transaction",
    "ctool4j-db",
    "ctool4j-mybatis-base",
    "ctool4j-mybatis-33",
    "ctool4j-mybatis-34",
    "ctool4j-mybatis",
    "ctool4j-feign",
    "ctool4j-job-base",
    "ctool4j-xxl-job",
    "ctool4j-csv",
    "ctool4j-excel",
    "ctool4j-minio",
    "ctool4j-doc-openapi2",

    "ctool4j-test-definition",
    "ctool4j-test-core",
    "ctool4j-test-spring",
)

dependencies {

    constraints {

        ctool4jArtifacts.forEach { artifactName ->
            if (null != rootProject.findProject(":$artifactName")) {
                // 记法必须是 ProjectDependency：把 Project 对象当约束记法在 Gradle 10 会直接报错
                api(project.dependencies.project(":$artifactName"))
            }
        }

    }

}
