
/**
 * <p>
 * Description: ctool4j 构建入口
 * </p>
 *
 * 模块发现：递归查找含 build.gradle.kts 的目录，按扁平逻辑名（:ctool4j-xxx）注册。
 * 按 JDK 档位切侧：jdk8 只用 javax 侧（排除全部 -jakarta 模块），其余只用 jakarta 侧（排除全部 -javax 模块）。
 * 档位取值：JDK_VERSION（系统属性 > 环境变量 > gradle.properties），默认 8；
 * 刻意不读 JVM 的 java.version 系统属性，否则默认档位会变成构建机 JDK。
 */

fun getConfigValue(key: String): String? {
    val value = System.getProperty(key)
    if (!value.isNullOrEmpty()) {
        return value
    }
    return System.getenv(key)
}

/** jdk8 档位：启用 javax 侧 */
val jdkVersion = (getConfigValue("JDK_VERSION") ?: "8").toInt()
val isJdk8 = 8 == jdkVersion
println("ctool4j settings: jdkVersion=$jdkVersion, side=${if (isJdk8) "javax" else "jakarta"}")

rootProject.name = "ctool4j"

/**
 * 构建产物目录与非模块目录，不参与模块发现。
 *
 * `target*` 覆盖 jdk8 档位的 `target-8`——产物目录按档位分家，两个档位的产物互不覆盖。
 */
val ignoredDirNames = setOf("buildSrc", "build", "target*", "tmp", "gradle", "doc", "script", "agent")

val baseDir = file(".")
baseDir.walk()
    .filter { dir ->
        val relativePath = dir.toRelativeString(baseDir).replace('\\', '/')
        dir.isDirectory
                && !dir.name.startsWith(".")
                && relativePath.split("/").none { it in ignoredDirNames }
                && !file("${dir.absolutePath}/settings.gradle.kts").exists()
                && file("${dir.absolutePath}/build.gradle.kts").exists()
    }
    .forEach { moduleDir ->

        val moduleName = moduleDir.name

        // 按 JDK 档位切换容器侧
        if (isJdk8 && moduleName.endsWith("-jakarta")) {
            return@forEach
        }
        if (!isJdk8 && moduleName.endsWith("-javax")) {
            return@forEach
        }

        val logicalModuleName = ":$moduleName"
        include(logicalModuleName)
        project(logicalModuleName).projectDir = moduleDir

    }
