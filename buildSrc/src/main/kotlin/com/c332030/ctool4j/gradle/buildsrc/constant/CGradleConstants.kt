package com.c332030.ctool4j.gradle.buildsrc.constant

/**
 * <p>
 * Description: CGradleConstants
 * </p>
 *
 * 构建脚本常量。
 *
 * @author c332030
 * @since 2026/9/26
 */

/** JDK 档位的配置键（系统属性 > 环境变量 > gradle.properties） */
const val JDK_VERSION = "JDK_VERSION"

/** JDK 8 档位取值 */
const val JDK8 = 8

/** 默认档位（用户要求：默认 jdk8） */
const val DEFAULT_JDK_VERSION = JDK8

/**
 * 版本目录中按档位区分的别名后缀（仅 jdk8 侧带后缀）。
 *
 * 最新 LTS 侧**不加后缀**——后续会升到更新的 LTS，用固定版本号（如 `-jdk25`）作后缀会过期。
 */
const val LIB_SUFFIX_JDK8 = "-jdk8"

/** 快照版本后缀 */
const val SNAPSHOT = "SNAPSHOT"

/** 容器侧的模块名后缀 */
const val SIDE_SUFFIX_JAVAX = "-javax"

/** 容器侧的模块名后缀 */
const val SIDE_SUFFIX_JAKARTA = "-jakarta"
