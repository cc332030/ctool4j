import org.gradle.api.tasks.compile.JavaCompile

plugins {
    `java-library`
}

dependencies {

    api(project(":ctool4j-processor-base"))

    // 测试编译触发自身注解处理器所需依赖（生成 service 接口）
    testImplementation(project(":ctool4j-mybatis"))

}

/**
 * 与迁移前的 Maven 配置对齐：测试编译时"清空注解处理器路径、由 classpath 自动发现处理器"。
 *
 * 迁移前 Maven 的 `annotationProcessorPaths combine.self="override"`（清空）即此语义：
 * 处理器从 classpath 扫描，故本模块自身的处理器（主输出）+ lombok + 处理器基座都可见。
 * Gradle 侧把处理器路径直接置为测试编译 classpath，等价且更明确。
 */
tasks.named<JavaCompile>("compileTestJava") {
    options.annotationProcessorPath = files(
        // 本模块自身输出（class + resources：处理器类、SPI 注册与模板资源）
        sourceSets.main.get().output,
        // 测试编译 classpath（lombok、处理器基座、mybatis-plus 等）
        configurations.named("testCompileClasspath")
    )
}
