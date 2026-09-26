package com.c332030.ctool4j.gradle.buildsrc.util

import com.c332030.ctool4j.gradle.buildsrc.constant.DEFAULT_JDK_VERSION
import com.c332030.ctool4j.gradle.buildsrc.constant.JDK8
import com.c332030.ctool4j.gradle.buildsrc.constant.JDK_VERSION
import org.gradle.api.Project

/**
 * <p>
 * Description: CGradleConfigUtils
 * </p>
 *
 * 构建配置读取工具。
 *
 * @author c332030
 * @since 2026/9/26
 */

/** 读取构建配置：系统属性 > 环境变量 > gradle.properties */
fun Project.getConfigValue(key: String): String? {
    return providers.systemProperty(key)
        .orElse(providers.environmentVariable(key))
        .orElse(providers.gradleProperty(key))
        .getOrNull()
}

/** 读取必需的构建配置，缺失即失败 */
fun Project.getRequireConfigValue(key: String): String {
    return getConfigValue(key)
        ?: throw IllegalArgumentException("$key is required")
}

/**
 * JDK 档位，缺省 [DEFAULT_JDK_VERSION]。
 *
 * 刻意不读 JVM 的 `java.version` 系统属性——它恒等于构建机 JDK，会使"默认 jdk8"失效。
 */
fun Project.getJdkVersion(): Int {
    return getConfigValue(JDK_VERSION)
        ?.toIntOrNull()
        ?: DEFAULT_JDK_VERSION
}

/** 是否 jdk8 档位 */
fun Project.isJdk8(): Boolean {
    return JDK8 == getJdkVersion()
}
