package com.c332030.ctool4j.gradle.buildsrc.util

import com.c332030.ctool4j.gradle.buildsrc.constant.LIB_SUFFIX_JDK8
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/**
 * <p>
 * Description: CGradleVersionCatalogUtils
 * </p>
 *
 * 版本目录读取工具：把"按 JDK 档位取依赖"收敛到本文件，模块构建脚本不出现 JDK 判断。
 *
 * @author c332030
 * @since 2026/9/26
 */

/**
 * Maven `provided` 作用域等价形态：编译期可见（不向下游传递），且本模块测试可见
 * （Maven 的 provided 在自身 test classpath 上）。
 */
fun DependencyHandler.cProvided(dependencyNotation: Any) {
    add("compileOnly", dependencyNotation)
    add("testImplementation", dependencyNotation)
}

/** Maven `optional` 作用域等价形态：编译期可见、不向下游传递，本模块测试可见 */
fun DependencyHandler.cOptional(dependencyNotation: Any) {
    add("compileOnly", dependencyNotation)
    add("testImplementation", dependencyNotation)
}

/** 取版本目录 `libs` */
fun Project.cVersionCatalog(): VersionCatalog {
    return rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
}

/**
 * 按 JDK 档位取依赖：jdk8 优先 `<别名>-jdk8`；最新 LTS 直接用 `<别名>`（无后缀），
 * 仅当 `<别名>` 不存在时才回退 `<别名>-jdk8`，以便"仅在 jdk8 侧存在差异"的场景也能表达。
 *
 * 约定见 `gradle/libs.versions.toml` 头部注释。
 */
fun Project.lib(alias: String): Provider<MinimalExternalModuleDependency> {

    val catalog = cVersionCatalog()
    val preferredAlias = if (isJdk8()) alias + LIB_SUFFIX_JDK8 else alias

    val preferred = catalog.findLibrary(preferredAlias)
    if (preferred.isPresent) {
        return preferred.get()
    }

    val fallback = catalog.findLibrary(alias)
    if (fallback.isPresent) {
        return fallback.get()
    }

    throw IllegalStateException("library alias: $preferredAlias / $alias is not defined!")

}

/**
 * 取版本目录中的版本值：jdk8 优先 `<别名>-jdk8`，最新 LTS 用无后缀的 `<别名>`（缺省回退 `<别名>`）。
 *
 * 用于按 group 对齐版本等"只需要版本号、不需要坐标"的场景。
 */
fun Project.libVersion(alias: String): String {

    val catalog = cVersionCatalog()
    val preferredAlias = if (isJdk8()) alias + LIB_SUFFIX_JDK8 else alias

    return (catalog.findVersion(preferredAlias).orElse(catalog.findVersion(alias).orElse(null)))
        ?.requiredVersion
        ?: throw IllegalStateException("version alias: $preferredAlias / $alias is not defined!")

}

/**
 * 把单个构件的版本对齐到指定值（`group:name` 精确匹配）。
 *
 * 用途：Gradle 在冲突时取"最高版本"，而 Maven 以 `dependencyManagement` 为准——
 * 上游 BOM 已用 `enforcedPlatform` 对齐，本函数用于"上游 BOM 未管理、但本项目取值与 Maven 不同"的构件
 *（如 guava：minio 传递声明 33.6.0-jre，Maven 取 ctool4j-bom 覆盖的 33.4.8-jre）。
 *
 * 用逐构件匹配而非按 group：同一 group 内各构件版本号可能独立（如 guava 与 failureaccess）。
 */
fun Project.cAlignModuleVersion(module: String, version: String) {

    val moduleParts = module.split(':')
    if (2 != moduleParts.size) {
        throw IllegalArgumentException("module must be 'group:name', actual: $module")
    }
    val (group, name) = moduleParts

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (target.group == group && target.name == name) {
                useVersion(version)
            }
        }
    }
}

/**
 * 声明"模块自带版本、且须压过 BOM 约束"的 api 依赖。
 *
 * 等价 Maven 语义：模块在自身 `<dependencies>` 里显式写出的版本胜于 `dependencyManagement`。
 * 采用 `strictly`：在非强约束平台上，Gradle 默认取"最高版本"，会把模块自带的版本顶掉。
 */
fun Project.cApiStrict(alias: String) {

    val dependency = lib(alias).get()
    val strictVersion = dependency.version
        ?: throw IllegalStateException("library alias: $alias has no version to strictly pin!")

    val strictDependency = dependencies.create(dependency) as ExternalModuleDependency
    strictDependency.version {
        strictly(strictVersion)
    }
    dependencies.add("api", strictDependency)

}
