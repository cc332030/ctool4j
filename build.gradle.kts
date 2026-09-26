import com.c332030.ctool4j.gradle.buildsrc.constant.SNAPSHOT
import com.c332030.ctool4j.gradle.buildsrc.util.*
import org.gradle.api.credentials.PasswordCredentials

/**
 * <p>
 * Description: ctool4j 根构建脚本
 * </p>
 *
 * 结构：
 * - 本文件承担"全仓统一配置"（等价于 Maven 的父 pom 继承）：档位判定、版本号、仓库、编译与测试选项、
 *   发布（含 sources/javadoc jar）、以及按"档位分组"注入的公共依赖。
 * - 各模块的 `build.gradle.kts` 只声明本模块专属依赖（等价于 Maven 各模块自己的 `<dependencies>`）。
 *
 * 档位分组（由目录归属判定，替代 Maven 继承层次）：
 * - `component`：`ctool4j-parent/ctool4j-component` 子树（对应 Maven `ctool4j-component`）
 * - `processor`：`ctool4j-processor-pom` 子树（对应 Maven `ctool4j-processor-pom`）
 * - `none`：其余（聚合/父 pom）
 *
 * 注意：Kotlin 的块注释可嵌套，注释与字符串里不得出现 `斜杠+星号` 连写（会开启嵌套注释）。
 */

// ==================== 基础信息 ====================

/** JDK 档位（默认 8）：决定容器侧与依赖版本档位 */
val jdkVersion = getJdkVersion()
val jdk8Profile = isJdk8()

val mainVersion = "2.0"
val dateVersion = "20260926"

/** 版本号：主版本 + 日期 + （仅 jdk8 的）-jdk8 + -SNAPSHOT */
val ctool4jVersion = buildString {
    append(mainVersion).append('.').append(dateVersion)
    if (jdk8Profile) {
        append("-jdk").append(jdkVersion)
    }
    append('-').append(SNAPSHOT)
}
val isSnapshot = true

val author = "c332030"
val authorGroup = "cc332030"
val authorEmail = "$author@$author.com"
val repoDomain = "github.com"
val authorGroupPath = "$repoDomain/$authorGroup"
val authorGroupUrl = "https://$authorGroupPath"
val gitProjectName = rootProject.name
val repoPath = "$authorGroupPath/$gitProjectName"
val repoUrl = "https://$repoPath"

val nexusUsername = getConfigValue("NEXUS_USERNAME")
val nexusPassword = getConfigValue("NEXUS_PASSWORD")
val nexusSnapshotId = getConfigValue("NEXUS_SNAPSHOT_ID")
val nexusSnapshotUrl = getConfigValue("NEXUS_SNAPSHOT_URL")
val nexusReleaseId = getConfigValue("NEXUS_RELEASE_ID")
val nexusReleaseUrl = getConfigValue("NEXUS_RELEASE_URL")

/** 最新 LTS 档位的字节码目标：Spring Boot 4 基线为 Java 17 */
val ltsRelease = 17

/**
 * 构建产物目录：**按档位分家**，两个档位的产物互不覆盖。
 *
 * - jdk8 档位：`target-8/build-8`
 * - 其余档位（最新 LTS）：`target/build`
 *
 * 不能用 Gradle 默认的 `build`：默认目录名与档位无关，切档位构建会覆盖上一次的产物。
 * 顶层目录是容器、末级 `build-8`/`build` 才是真正的构建目录（与 Maven 的 `target` 约定同形）。
 *
 * **分层目录（`ctool4j-parent/ctool4j-component/...`）用目录名而非路径**：Gradle 的 `projectDir` 是相对路径，
 * 顶层能解析、`subprojects` 里直接 `file(...)` 会以「调用者所在项目」为基准而解析失败——
 * 按目录名就近放置，既避开这个坑，路径也短。
 */
val targetDirName = if (jdk8Profile) "target-8" else "target"
val buildDirName = if (jdk8Profile) "build-8" else "build"

/**
 * 聚合/父 pom 模块（packaging=pom）。
 *
 * 按迁移前的 packaging 逐模块钉定——**不能用"有无 src 目录"判定**：存在无源码但打 jar 的模块
 * （`ctool4j-mq-base`、`ctool4j-rabbitmq`、`ctool4j-rocketmq`、`ctool4j-nacos-config`、
 * `ctool4j-test-core`、`ctool4j-test-spring`、`ctool4j-mq-processor`）。
 */
val pomModuleNames = setOf(
    "ctool4j-bom",
    "ctool4j-dependencies",
    "ctool4j-processor-pom",
    "ctool4j-parent",
    "ctool4j-pom",
    "ctool4j-boot-parent",
    "ctool4j-processor-parent",
    "ctool4j-component",
    "ctool4j-auth-pom",
    "ctool4j-doc-pom",
    "ctool4j-file-pom",
    "ctool4j-http-pom",
    "ctool4j-job-pom",
    "ctool4j-log-pom",
    "ctool4j-mq-pom",
    "ctool4j-mybatis-pom",
    "ctool4j-nacos-pom",
    "ctool4j-spring-base-pom",
    "ctool4j-spring-pom",
    "ctool4j-test-pom"
)

/**
 * 自带依赖版本、且须压过 ctool4j-bom 约束的模块。
 *
 * `ctool4j-mybatis-33` / `-34` 固定在 mybatis-plus 3.3.2 / 3.4.3.4（版本本身即模块语义），
 * 而 ctool4j-bom 管理的是 3.5.16——故这两个模块不挂"强约束的 ctool4j-bom"，
 * 改用普通 platform 叠加 + 自身依赖 `strictly`（复刻 Maven"模块显式声明胜于 dependencyManagement"）。
 */
val ownVersionModuleNames = setOf(
    "ctool4j-mybatis-33",
    "ctool4j-mybatis-34"
)

/**
 * 参与发布的配置（其依赖会写进产出 pom / module metadata）：
 * 这里只能用普通 `platform`——`enforcedPlatform` 会泄漏给消费者，Gradle 直接拒绝发布这类变体。
 */
val publishedPlatformConfigurationNames = listOf(
    "api",
    "implementation"
)

/**
 * 不参与发布的配置（编译期与测试期）：
 * 这里用 `enforcedPlatform`（强约束 = Maven 的 dependencyManagement）；
 * compileClasspath / testCompileClasspath / testRuntimeClasspath 都会带上这些约束，故实际编译与测试解析结果以强约束为准。
 */
val enforcedPlatformConfigurationNames = listOf(
    "compileOnly",
    "testCompileOnly",
    "testImplementation",
    "annotationProcessor",
    "testAnnotationProcessor"
)

group = "com.c332030"
version = ctool4jVersion
description = "CTool for Java"

println(
    "ctool4j build config: jdk=$jdkVersion, side=${if (jdk8Profile) "javax" else "jakarta"}, " +
            "version=$ctool4jVersion"
)

// ==================== 构建产物布局 ====================

/** 根项目：与各模块同构（`target-8/build-8` 或 `target/build`），生成物不放默认的 `build` */
layout.buildDirectory.set(file("$targetDirName/$buildDirName"))

// ==================== 子项目统一配置 ====================

subprojects {

    val moduleProject = this
    val projectName = name

    group = rootProject.group
    version = rootProject.version

    /**
     * 构建产物布局：顶层（根项目）与各模块同构，都是「项目目录下的 `target-8/build-8`（jdk8）或 `target/build`」。
     *
     * 用**目录名**而非 `projectDir` 拼路径——Gradle 的 `projectDir` 是相对路径，顶层能解析、
     * `subprojects` 里解析会以「调用者所在项目」为基准而失败；按目录名就近放置既避开这个坑，路径也短。
     */
    layout.buildDirectory.set(moduleProject.file("$targetDirName/$buildDirName"))

    /** 聚合/父 pom：只发布 pom，不参与编译 */
    val isPomOnly = pomModuleNames.contains(projectName)
    /** BOM 模块：以 java-platform 发布 constraints */
    val isBomModule = "ctool4j-bom" == projectName || "ctool4j-dependencies" == projectName
    val isJarModule = !isPomOnly && !isBomModule

    apply(plugin = "maven-publish")
    when {
        isBomModule -> apply(plugin = "java-platform")
        isJarModule -> apply(plugin = "java-library")
        // 聚合/父 pom 同样是 java-platform：原生产出 `<packaging>pom</packaging>` 且不产 jar，
        // 不再需要改写 pom 的 `<packaging>`（原先的 cSetPackaging 属 Maven 模拟）
        else -> apply(plugin = "java-platform")
    }

    repositories {
        configureSharedRepositories(moduleProject)
    }

    /**
     * 档位分组：模块目录归属即 Maven 继承归属。
     *
     * `springPom` / `mybatisPom` 是 `component` 的子档位——这两个聚合 pom 自身带有 provided 依赖，
     * 会一并传给其子模块（如 `ctool4j-spring-pom` 的 `spring-boot-starter-test`）。
     */
    val modulePath = moduleProject.projectDir.toRelativeString(rootDir).replace('\\', '/')
    val flavor = when {
        modulePath.startsWith("ctool4j-processor-pom") -> "processor"
        modulePath.startsWith("ctool4j-parent/ctool4j-component/ctool4j-spring-pom") -> "springPom"
        modulePath.startsWith("ctool4j-parent/ctool4j-component/ctool4j-mybatis-pom") -> "mybatisPom"
        modulePath.startsWith("ctool4j-parent/ctool4j-component") -> "component"
        else -> "none"
    }

    if (isJarModule) {

        extensions.configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(jdkVersion))
            }
        }

        // ---- 档位专属源码目录 ----
        // 少数类在 jdk8 与最新 LTS 档位下必须用不同的底层 API（如 HttpClient 4 vs HttpClient 5：
        // Boot 4 / Spring 7 的 HttpComponentsClientHttpRequestFactory 只接受 HttpClient 5），
        // 无法用同一份源码兼容。约定：差异实现按档位分放 `src/<main|test>/java-jdk8` 与 `.../java-latest`
        //（同包同名、互斥），由档位决定纳入哪一个；两目录都不放"公共类"，故不存在同名覆盖问题。
        // jdk8 档位只读 java-jdk8，非 jdk8 档位只读 java-latest，各自产物互不影响。
        // main 与 test 两套源码目录均适用本规则（仅当目录存在时才追加）。
        val jdkSideSuffix = if (jdk8Profile) "jdk8" else "latest"
        listOf("main", "test").forEach { sourceSetName ->
            val variantSourceDir = moduleProject.file("src/$sourceSetName/java-$jdkSideSuffix")
            if (variantSourceDir.exists()) {
                moduleProject.extensions.getByType<JavaPluginExtension>()
                    .sourceSets
                    .named(sourceSetName)
                    .configure {
                        java.srcDir(variantSourceDir)
                    }
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            // 与 Maven `<parameters>true</parameters>` 对齐：Spring 按参数名注入需要
            options.compilerArgs.add("-parameters")
            if (!jdk8Profile) {
                options.release.set(ltsRelease)
            }
        }

        tasks.withType<Javadoc>().configureEach {
            options.encoding = "UTF-8"
            // 与 Maven javadoc 插件的 <failOnError>true</failOnError> / <quiet>true</quiet> 对齐
            isFailOnError = true
            (options as CoreJavadocOptions).quiet()
        }

        // 资源：不做构建期过滤（Maven 侧虽开了 `<filtering>true</filtering>`，但全量核查后没有任何资源
        // 用到 ${project.*} / ${java.version} 这类构建期占位符；src/main/resources 下的 ${...} 全部是
        // logback/log4j/FreeMarker 的运行期占位符，必须原样保留），也不再为兼容 Maven 的
        // "src/main/java 下 xml 也算资源" 而额外挂资源目录——仓库内该目录下没有 xml，属 Maven 侧的死配置。

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            // 性能测试默认不执行（命名契约：*PerfTests）；-Pperf 显式放开
            if (!moduleProject.hasProperty("perf")) {
                filter {
                    excludeTestsMatching("*PerfTests")
                }
            }
        }

        // ---- 与 Maven dependencyManagement 对齐（逐项）----
        // 上游 BOM 已用 enforcedPlatform 对齐；此处处理"本项目在版本目录里显式覆盖"的构件。
        // lombok：上游 BOM 也管理它，但本项目覆盖（jdk8 与最新 LTS 各有取值），按目录值对齐
        cAlignModuleVersion("org.projectlombok:lombok", libVersion("lombok"))
        if (jdk8Profile) {
            // ctool4j-bom 显式覆盖：minio 传递声明 guava 33.6.0-jre，Maven 取 BOM 的 33.4.8-jre；
            // 该 group 内 failureaccess / listenablefuture 版本号独立，故逐构件对齐
            cAlignModuleVersion("com.google.guava:guava", libVersion("guava"))
        }

        dependencies {

            // ---- 平台约束 ----
            // 上游 BOM 用 enforcedPlatform：等价 Maven 的 dependencyManagement（以 BOM 为准，而非取"最高版本"）。
            // 非强约束时会与 Maven 结果不一致（实测：POI 传递声明 log4j 2.21.1 而 Boot 管理值 2.17.2、
            //  minio 传递声明 jackson 2.21.2 而 Boot 管理值 2.13.5、okhttp 传递声明 kotlin 1.9.10 而 Boot 管理值 1.6.21，
            //  Maven 全部取 BOM 值）。
            // ctool4j-bom 以普通 platform 叠加：它带来本项目对第三方的显式覆盖（如 guava 33.4.8-jre），
            // 这些覆盖值高于上游 BOM 的对应值，故不受强约束影响；与上游 BOM 管理值**同名同版本**时也不冲突。
            val platformSpecs = listOf(
                lib("spring-boot-dependencies"),
                lib("spring-cloud-dependencies"),
                lib("spring-cloud-alibaba-dependencies")
            )

            enforcedPlatformConfigurationNames.forEach { configurationName ->
                platformSpecs.forEach { add(configurationName, enforcedPlatform(it)) }
                // 自带版本模块不挂强约束的 ctool4j-bom（否则其 pinned 版本会被 BOM 顶掉）
                if (!ownVersionModuleNames.contains(projectName)) {
                    add(configurationName, enforcedPlatform(project(":ctool4j-bom")))
                }
            }

            publishedPlatformConfigurationNames.forEach { configurationName ->
                platformSpecs.forEach { add(configurationName, platform(it)) }
                add(configurationName, platform(project(":ctool4j-bom")))
            }

            // 说明：本脚本未 apply JVM 插件，故不使用 `compileOnly(...)` 一类的访问器，一律走 add(配置名, 记法)

            /** 同一记法同时挂到多个配置 */
            fun addToAll(configurationNames: List<String>, dependencyNotation: Any) {
                configurationNames.forEach { add(it, dependencyNotation) }
            }

            // ---- 全仓公共（对应 Maven ctool4j-parent / ctool4j-processor-pom 的公共依赖）----
            addToAll(
                listOf("compileOnly", "annotationProcessor", "testCompileOnly", "testAnnotationProcessor"),
                lib("lombok")
            )

            add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
            add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
            if (!jdk8Profile) {
                // Boot 4 的 starter-test 不再传递 spring-boot-webmvc-test（AutoConfigureMockMvc 等已迁至
                // org.springframework.boot.webmvc.test.autoconfigure）；jdk8 的 Boot 2.7 由 starter-test 自带，无需额外声明
                add("testImplementation", "org.springframework.boot:spring-boot-webmvc-test")
            }

            /** 对应 Maven ctool4j-component：provided + optional（其下全部子模块继承） */
            fun componentDependencies() {
                addToAll(listOf("compileOnly", "testImplementation"), "org.springframework.boot:spring-boot-starter-web")
                addToAll(listOf("compileOnly", "testImplementation"), "org.springframework.boot:spring-boot-starter-validation")
                addToAll(listOf("compileOnly", "testImplementation"), "org.springframework.boot:spring-boot-starter-logging")
                addToAll(listOf("compileOnly", "testImplementation"), "org.springframework.cloud:spring-cloud-commons")
                addToAll(listOf("compileOnly", "testImplementation"), "org.springframework.cloud:spring-cloud-context")
                addToAll(listOf("compileOnly", "testImplementation"), lib("guava"))
                addToAll(listOf("compileOnly", "testImplementation"), lib("mapstruct"))

                // Jackson 2（com.fasterxml.jackson）：Boot 2.7 的 starter-web 原生带它；
                // Boot 4 起 starter-web 改用 Jackson 3（tools.jackson），Jackson 2 不再传递。
                // 本仓库 Jackson 层（CJacksonUtils、各序列化器、CSecurityJacksonModule 等）按 Jackson 2 编写，
                // 故显式引入、两档位都保留；版本由各自平台的 Jackson 2 BOM 提供（Boot 4 仍 import com.fasterxml.jackson:jackson-bom）。
                addToAll(listOf("compileOnly", "testImplementation"), lib("jackson-databind"))
                addToAll(listOf("compileOnly", "testImplementation"), lib("jackson-datatype-jsr310"))

                // HttpClient 5：仅最新 LTS 档位（Spring 7 的 HttpComponentsClientHttpRequestFactory 只接受 HC5）；
                // jdk8 档位沿用 HttpClient 4（经 httpmime 传递），不注入本依赖
                if (!jdk8Profile) {
                    addToAll(listOf("compileOnly", "testImplementation"), lib("httpclient5"))
                }

                // 对应 Maven ctool4j-parent：@CAutowired 注解处理器（provided + annotationProcessorPaths）
                val autowiredProcessor = project(":ctool4j-autowired-processor")
                addToAll(listOf("compileOnly", "annotationProcessor", "testImplementation"), autowiredProcessor)
            }

            when (flavor) {

                "component" -> componentDependencies()

                "springPom" -> {
                    // 对应 Maven ctool4j-spring-pom 自身的 provided 依赖
                    addToAll(listOf("compileOnly", "testImplementation"), "org.springframework.boot:spring-boot-starter-test")
                    componentDependencies()
                }

                "mybatisPom" -> {
                    // 对应 Maven ctool4j-mybatis-pom 自身的 provided 依赖
                    addToAll(listOf("compileOnly", "testImplementation"), "org.springframework.boot:spring-boot-starter-jdbc")
                    componentDependencies()
                }

                "processor" -> {
                    // 对应 Maven ctool4j-processor-pom：starter-web / starter-test 为 test 作用域
                    add("testImplementation", "org.springframework.boot:spring-boot-starter-web")
                }

            }

        }

    }

    // ==================== 发布 ====================

    /**
     * 聚合/父 pom 的 Maven 继承载荷。
     *
     * 这些构件对外的用途就是被业务系统当 `<parent>` 继承，而 Maven 的继承是"内容级"的：
     * Gradle 产出的 pom 不带继承链（按 Gradle 规范也不该带），故把迁移前 Maven pom 的**有效语义**
     * 直接写进产出 pom。
     *
     * `ctool4j-parent` 与 `ctool4j-processor-parent` 的差异正在此处——后者多出 mybatis / mq
     * 两个注解处理器（既作为 provided 依赖继承，也进 `annotationProcessorPaths`）。
     */
    val publishGroupId = moduleProject.group.toString()
    val lombokCoordinate = "org.projectlombok:lombok:${libVersion("lombok")}"
    val autowiredProcessorCoordinate = "$publishGroupId:ctool4j-autowired-processor:$ctool4jVersion"
    val mybatisProcessorCoordinate = "$publishGroupId:ctool4j-mybatis-processor:$ctool4jVersion"
    val mqProcessorCoordinate = "$publishGroupId:ctool4j-mq-processor:$ctool4jVersion"
    val compilerJavaVersion = if (jdk8Profile) jdkVersion.toString() else ltsRelease.toString()
    val springBootPluginVersion = lib("spring-boot-dependencies").get().version
        ?: throw IllegalStateException("spring-boot-dependencies version is not defined")

    val inheritedMavenContent: MavenParentContent? = when (projectName) {

        // 业务系统的公共父 pom：BOM 版本管理 + lombok/autowired 处理器（provided）+ 测试依赖 + 编译器配置
        "ctool4j-parent", "ctool4j-pom" -> MavenParentContent(
            bomImport = "$publishGroupId:ctool4j-bom:$ctool4jVersion",
            providedDependencies = listOf(lombokCoordinate, autowiredProcessorCoordinate),
            testDependencies = listOf("org.springframework.boot:spring-boot-starter-test"),
            compilerJavaVersion = compilerJavaVersion,
            compilerParameters = true,
            annotationProcessorPaths = listOf(lombokCoordinate, autowiredProcessorCoordinate),
            springBootPluginVersion = springBootPluginVersion
        )

        // 业务应用的父 pom：同 ctool4j-parent，插件以 <plugins> 形式给出（见 cAddSpringBootPlugin）
        "ctool4j-boot-parent" -> MavenParentContent(
            bomImport = "$publishGroupId:ctool4j-bom:$ctool4jVersion",
            providedDependencies = listOf(lombokCoordinate, autowiredProcessorCoordinate),
            testDependencies = listOf("org.springframework.boot:spring-boot-starter-test"),
            compilerJavaVersion = compilerJavaVersion,
            compilerParameters = true,
            annotationProcessorPaths = listOf(lombokCoordinate, autowiredProcessorCoordinate)
        )

        // 注解处理器父 pom：比 ctool4j-parent 多 mybatis / mq 处理器
        "ctool4j-processor-parent" -> MavenParentContent(
            bomImport = "$publishGroupId:ctool4j-bom:$ctool4jVersion",
            providedDependencies = listOf(
                lombokCoordinate,
                autowiredProcessorCoordinate,
                mybatisProcessorCoordinate,
                mqProcessorCoordinate
            ),
            testDependencies = listOf("org.springframework.boot:spring-boot-starter-test"),
            compilerJavaVersion = compilerJavaVersion,
            compilerParameters = true,
            annotationProcessorPaths = listOf(
                lombokCoordinate,
                autowiredProcessorCoordinate,
                mybatisProcessorCoordinate,
                mqProcessorCoordinate
            ),
            springBootPluginVersion = springBootPluginVersion
        )

        else -> null
    }

    extensions.configure<PublishingExtension> {

        publications {

            val publicationName = if (isPomOnly) "mavenPom" else "mavenJava"
            create<MavenPublication>(publicationName) {

                if (isJarModule) {
                    from(components["java"])
                } else {
                    // BOM 与聚合/父 pom：java-platform 组件（packaging=pom、无 jar）
                    from(components["javaPlatform"])
                }

                pom {

                    url.set(repoUrl)

                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set(author)
                            name.set(author)
                            email.set(authorEmail)
                            url.set(authorGroupUrl)
                        }
                    }

                    scm {
                        connection.set("scm:git:git@${repoDomain}:${authorGroup}/${gitProjectName}.git")
                        url.set(repoUrl)
                        tag.set("HEAD")
                    }

                }

                // 聚合/父 pom 写入 Maven 继承载荷（空壳会让业务系统失去迁移前的继承语义）
                if (null != inheritedMavenContent) {
                    cAddInheritedMavenContent(inheritedMavenContent)
                }

            }

        }

        /**
         * 发布仓库：**注册的唯一门槛是「配置了地址」**。
         *
         * 凭据不能作为注册条件——本项目凭据取自环境变量，本地/CI 只要没注入这两个变量，
         * 仓库就整个不注册：`publish` 变成无任何 actions 的空任务，**静默跳过、构建照旧 success**。
         * 这正是「构建成功但制品一个都没上传」的成因，故地址在就注册，凭据缺了交给发布任务如实报错。
         *
         * 凭据见下：优先**延迟解析**（`credentials(PasswordCredentials::class)`，按 `<仓库名>Username` /
         * `<仓库名>Password` 属性查找，与配置缓存共存）；没有对应属性时回退到
         * `NEXUS_USERNAME` / `NEXUS_PASSWORD` 的显式凭据——该组合下 Gradle 会以
         * "Explicit credentials are unsupported with the Configuration Cache" 为由禁用本次构建的配置缓存。
         */
        val repositoryUrl = if (isSnapshot) nexusSnapshotUrl else nexusReleaseUrl
        // Gradle 的延迟凭据按「仓库名 + Username/Password」查属性，且强制仓库名只能含**字母与数字**
        // （否则报 "Identity may contain only letters and digits"，下划线同样不行），故把配置里的 id 归一化：
        // `cc332030_snapshot` → `cc332030snapshot`。
        val repositoryId = (if (isSnapshot) nexusSnapshotId else nexusReleaseId)
            ?.replace(Regex("[^A-Za-z0-9]"), "")
            ?.takeIf { it.isNotEmpty() }
        val repositoryName = repositoryId ?: "nexus"

        if (!repositoryUrl.isNullOrEmpty()) {
            /**
             * 凭据两档：
             * 1. **延迟凭据**（首选）：属性 `<仓库名>Username` / `<仓库名>Password`，可由 `-P`、
             *    `gradle.properties` 或环境变量 `ORG_GRADLE_PROJECT_<仓库名>Username` 提供——
             *    与配置缓存共存。
             * 2. **回退**：`NEXUS_USERNAME` / `NEXUS_PASSWORD` 的显式凭据。Gradle 不支持
             *    "显式凭据 + 配置缓存"（Reason: Explicit credentials are unsupported with the
             *    Configuration Cache），会因此禁用本次构建的配置缓存。
             */
            val credentialPropertyName = "${repositoryName}Username"
            val lazyCredentialsPresent = providers.gradleProperty(credentialPropertyName).isPresent
                || providers.environmentVariable("ORG_GRADLE_PROJECT_$credentialPropertyName").isPresent

            repositories {
                maven {
                    name = repositoryName
                    url = uri(repositoryUrl)
                    if (lazyCredentialsPresent) {
                        credentials(PasswordCredentials::class)
                    } else if (!nexusUsername.isNullOrEmpty() && !nexusPassword.isNullOrEmpty()) {
                        credentials {
                            username = nexusUsername
                            password = nexusPassword
                        }
                    }
                }
            }
        }

    }

    /**
     * 可选用能力（Gradle 的 `compileOnly`）：迁移前 Maven 侧用 `provided` / `optional` 表达，
     * 按用户要求**统一改用 `optional`**——`compileOnly` 不进 Gradle 的发布元数据，故把它的依赖写进产出 pom
     * 并标 `<optional>true</optional>`（不写 scope，默认 compile）：使用方能看到"可选用能力"与解析后的版本，
     * 且这些依赖仍不向下游传递。
     *
     * 时机：模块自身的 `build.gradle.kts`（`cOptional` / `cProvided` 声明）晚于本 `subprojects` 块执行，
     * 早读会得到空集，故取值放在 `afterEvaluate`。传给发布配置的只有纯字符串、（不捕获 Project），
     * 以保持 configuration-cache 可用（与 `CGradlePublishUtils` 的既有约定一致）。
     */
    moduleProject.afterEvaluate {

        val optionalCoordinates = moduleProject.configurations.findByName("compileOnly")
            ?.dependencies
            ?.filter { dependency ->
                // 排除 BOM：`platform` / `enforcedPlatform` 依赖同样挂在本配置上，但它们属版本管理
                // （Gradle 会翻译成 pom 的 `dependencyManagement` import），若当普通依赖写成 optional，
                // 使用方会把它当可选用依赖引入——语义错、还可能引入冲突。
                val categoryName = (dependency as? ModuleDependency)
                    ?.attributes
                    ?.getAttribute(Category.CATEGORY_ATTRIBUTE)
                    ?.name
                // BOM 有两种形态：`platform(...)` → `platform`，`enforcedPlatform(...)` → `enforced-platform`
                categoryName?.contains("platform") != true
            }
            ?.mapNotNull { dependency ->
                // 声明里可能不带版本（版本由 BOM 管理，如根脚本按目录归属注入的 starter-web 一类的 `g:a`）：
                // 产物 pom 也写成不带版本——使用方按 BOM 解析，与迁移前 Maven pom 的写法一致。
                val group = dependency.group ?: return@mapNotNull null
                val version = dependency.version
                if (version.isNullOrEmpty()) "$group:${dependency.name}" else "$group:${dependency.name}:$version"
            }
            ?.distinct()
            ?.sorted()
            .orEmpty()

        if (optionalCoordinates.isNotEmpty()) {
            val publicationName = if (isPomOnly) "mavenPom" else "mavenJava"
            moduleProject.extensions.configure<PublishingExtension> {
                publications.named<MavenPublication>(publicationName) {
                    cAddOptionalDependencies(optionalCoordinates)
                }
            }
        }
    }

    // ctool4j-boot-parent 对外是业务应用的 Maven 父 pom：注入 spring-boot-maven-plugin 配置
    if ("ctool4j-boot-parent" == projectName) {
        val springBootVersion = lib("spring-boot-dependencies").get().version
            ?: throw IllegalStateException("spring-boot-dependencies version is not defined")
        extensions.configure<PublishingExtension> {
            publications.named<MavenPublication>("mavenPom") {
                cAddSpringBootPlugin(springBootVersion)
            }
        }
    }

}
