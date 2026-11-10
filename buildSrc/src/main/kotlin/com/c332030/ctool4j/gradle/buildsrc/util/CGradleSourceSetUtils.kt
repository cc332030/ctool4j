package com.c332030.ctool4j.gradle.buildsrc.util

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

/**
 * <p>
 * Description: CGradleSourceSetUtils
 * </p>
 *
 * 档位专属源码目录的「整体替换」形态（详见根 `build.gradle.kts` 的「档位专属源码目录」段）。
 *
 * @author c332030
 * @since 2026/11/10
 */

/**
 * jdk8 档位用「另一侧目录」**整体替换**主源集，其余档位保持 `src/<set>/java` 不变。
 *
 * 根脚本对 `src/<set>/java-<side>` 是**追加**语义，适用于「档位目录放的是与 `src/<set>/java`
 * 互不相交的类」（如 `ctool4j-core`）。但有些模块的 `src/<set>/java` 与另一侧目录承载的是
 * **同一个全限定名的两份实现**（如 SLF4J 1.7/2.0 的 `CMdcLogback`、javax/jakarta 的
 * `CHttpServletRequest`），追加必然重复——同包同名的两份 class 同时进 classpath 时 JVM 取先命中者
 * 且**不报错**，属不可控行为，故必须替换。
 *
 * **只在自己所属的档位生效**：目录名写成 `java-jdk8` / `java-javax` 就只认 jdk8 档位，
 * 最新 LTS 档位仍编 `src/<set>/java`（默认侧）。这一步不能省——`java-javax` 目录在两档位下都存在，
 * 无条件替换会让最新 LTS 档位去编 javax 侧、直接编译失败。
 *
 * 因此**每个模块都有 `src/<set>/java`**：它要么是唯一主源目录（不替换时），要么是「默认侧」的
 * 实现目录（最新 LTS / jakarta）。
 *
 * 必须在模块自己的 `build.gradle.kts` 里调用：根脚本的 `subprojects {}` 先执行（先追加），
 * 模块脚本后执行（`setSrcDirs` 覆盖），顺序由 Gradle 的求值次序保证。
 *
 * @param alternativeDirName 另一侧目录名（相对 `src/<set>`），如 `java-jdk8`、`java-javax`
 * @param sourceSetName 源集名，默认 `main`
 */
fun Project.cExclusiveJdk8SourceDirs(alternativeDirName: String, sourceSetName: String = "main") {

    if (!isJdk8()) {
        return
    }

    val alternativeDir = file("src/$sourceSetName/$alternativeDirName")
    if (!alternativeDir.exists()) {
        return
    }

    extensions.getByType(SourceSetContainer::class.java)
        .named(sourceSetName)
        .configure { java.setSrcDirs(listOf(alternativeDir)) }
}
