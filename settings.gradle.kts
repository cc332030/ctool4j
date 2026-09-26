
/**
 * <p>
 * Description: ctool4j 构建入口
 * </p>
 *
 * 模块发现：递归查找含 build.gradle.kts 的目录，按扁平逻辑名（:ctool4j-xxx）注册。
 * 容器侧（javax / jakarta）**不再按模块拆**：侧适配类合入同一模块、按 JDK 档位选用源码目录
 * （`src/main/java-javax` 与 `src/main/java-jakarta`，见根 `build.gradle.kts`），故此处不做侧过滤。
 * 仍有一个「只有 javax 形态、无法在两档位共存」的模块，按白名单在非 jdk8 档位排除（见 javaxOnlyModuleNames）。
 * 档位取值：JDK_VERSION（系统属性 > 环境变量 > gradle.properties），默认 8；
 * 刻意不读 JVM 的 java.version 系统属性，否则默认档位会变成构建机 JDK。
 */

/**
 * JDK 工具链自动下载（Gradle 官方 foojay 解析器）。
 *
 * 本项目按档位需要不同 JDK（jdk8 档用 8、最新 LTS 档用 17+），而构建机往往只装一个 JDK：
 * 本机已装则直接探测到、不下载；缺失时由 Gradle 自动取到 `~/.gradle/jdks`
 * （CI 侧该目录已挂缓存卷，装一次后由缓存复用）。依据官方 toolchain 文档。
 */
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

/**
 * 读取构建配置：系统属性 > 环境变量 > gradle.properties。
 *
 * settings 脚本**不能**引用 buildSrc 的 `getConfigValue`（settings 的构建早于 buildSrc 的类产出），
 * 故这里保留一份实现；三档来源与 buildSrc 版**必须一致**——少一档会让 `gradle.properties` 里的
 * `JDK_VERSION` 静默失效（settings 按 jdk8 筛模块、根脚本按另一档取依赖，两处错位）。
 * 改动其一时务必同步另一处（`buildSrc` 的 `CGradleConfigUtils.getConfigValue`）。
 */
fun getConfigValue(key: String): String? {
    return settings.providers.systemProperty(key)
        .orElse(settings.providers.environmentVariable(key))
        .orElse(settings.providers.gradleProperty(key))
        .getOrNull()
        ?.takeIf { it.isNotEmpty() }
}

/** jdk8 档位：启用 javax 侧 */
val jdkVersion = (getConfigValue("JDK_VERSION") ?: "8").toInt()
val isJdk8 = 8 == jdkVersion

/**
 * javax 专属模块白名单（仅在 jdk8 档位纳入）。
 *
 * 适用于「底层技术栈只有 javax 形态、根本没有 jakarta 版本」的模块——
 * 这类模块无法靠 `-javax` / `-jakarta` 后缀区分（加了后缀会改掉 jdk8 档位既有的 artifact 坐标，
 * 违反"jdk8 档位产物零变化"的原则），故在此按名单在非 jdk8 档位排除。
 *
 * `ctool4j-doc-openapi2`：基于 Springfox（OpenAPI2），而 Springfox 早已停更于 Spring 5 / javax 时代，
 * 不存在支持 jakarta 的版本（knife4j 的 OpenAPI3 是另一套 API，非本模块的替代实现）。
 * 与容器适配模块不同：它的 javax 依赖是**第三方库**给的，无法靠自身换源码目录切到 jakarta，
 * 故仍需要模块级排除。
 */
val javaxOnlyModuleNames = setOf(
    "ctool4j-doc-openapi2"
)
println("ctool4j settings: jdkVersion=$jdkVersion, containerSide=${if (isJdk8) "javax" else "jakarta"}")

rootProject.name = "ctool4j"

/**
 * 构建产物目录与非模块目录，不参与模块发现。
 *
 * **必须剪枝而不是只做候选过滤**：`walk()` 是"枚举全树再筛"，产物目录（`target`、`target-8`、
 * `build`、`build-8`）里的成千上万个产物文件照样会被读入——它们每次构建都在变，
 * 既拖慢配置，又让配置缓存条目次次失效（Gradle 把这些读取记为配置期输入）。
 *
 * `target` / `build` 按**前缀**排除：产物目录名带档位后缀（jdk8 → `target-8`/`build-8`），
 * 精确名集合匹配不到它们——这是上一版 `"target*"` 写在 `setOf` 里形同虚设的根因。
 *
 * `src` 也剪掉：源码树里不会有 Gradle 模块，逐目录做 `build.gradle.kts` 存在性检查同样是配置期输入，
 * 新增一个包目录就会让配置缓存失效。
 */
val ignoredDirPrefixes = listOf(".", "target", "build")
val ignoredDirNames = setOf("buildSrc", "tmp", "gradle", "doc", "script", "agent", "src")

fun isIgnoredDir(dir: File): Boolean {
    val name = dir.name
    return name.isEmpty() || ignoredDirNames.contains(name) || ignoredDirPrefixes.any { name.startsWith(it) }
}

val baseDir = file(".")
baseDir.walkTopDown()
    .onEnter { !isIgnoredDir(it) }
    .filter { dir ->
        dir.isDirectory
                && !file("${dir.absolutePath}/settings.gradle.kts").exists()
                && file("${dir.absolutePath}/build.gradle.kts").exists()
    }
    .forEach { moduleDir ->

        val moduleName = moduleDir.name

        // 容器侧不按模块拆（含 javax/jakarta 源码目录的模块按档位选源目录），
        // 只有「无 jakarta 形态的 javax 专属模块」需要在非 jdk8 档位排除
        if (!isJdk8 && moduleName in javaxOnlyModuleNames) {
            return@forEach
        }

        val logicalModuleName = ":$moduleName"
        include(logicalModuleName)
        project(logicalModuleName).projectDir = moduleDir

    }
