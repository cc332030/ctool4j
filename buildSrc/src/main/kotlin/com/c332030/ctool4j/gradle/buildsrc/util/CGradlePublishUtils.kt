package com.c332030.ctool4j.gradle.buildsrc.util

import groovy.util.Node
import groovy.xml.XmlNodePrinter
import groovy.xml.XmlParser
import org.gradle.api.publish.maven.MavenPublication
import java.io.PrintWriter
import java.io.StringWriter

/**
 * <p>
 * Description: CGradlePublishUtils
 * </p>
 *
 * 发布相关工具：pom 的 XML 级增补（`<packaging>`、`ctool4j-boot-parent` 的插件配置）。
 *
 * 说明：
 * - **不再注入 `<parent>`**：Gradle 产出的 pom 本身自洽——依赖版本由 pom 内 `<dependencyManagement>`
 *   的 BOM import 提供（`platform(...)` 依赖会被 Gradle 翻译成 import），无需模拟 Maven 的父子继承链。
 * - 以下函数的参数一律用普通值（String）而非 Project——`pom.withXml` 的回调在任务执行期执行，
 *   闭包捕获 Project 会破坏 Gradle 的 configuration-cache。
 * - 改写 pom 走 XML 字符串（XmlParser → 增补 → XmlNodePrinter），不使用 `XmlProvider.asNode()`
 *   （该 DSL 扩展在 buildSrc 中不可用）。
 *
 * @author c332030
 * @since 2026/9/26
 */

/** 解析 pom XML 文本为节点树 */
private fun String.toPomNode(): Node = XmlParser(false, false).parseText(this) as Node

/** 节点树还原为 XML 文本 */
private fun Node.toPomString(): String {
    val writer = StringWriter()
    XmlNodePrinter(PrintWriter(writer)).print(this)
    return writer.toString()
}

/**
 * 往产出 pom 里注入 `<build><plugins>`（当前仅 `ctool4j-boot-parent` 需要）：
 * 该构件对外是业务应用的 Maven 父 pom，须带 spring-boot-maven-plugin 的版本与 repackage 执行。
 */
fun MavenPublication.cAddSpringBootPlugin(springBootVersion: String) {

    pom.withXml {

        val builder = asString()
        val root = builder.toString().toPomNode()
        val build = root.childOrCreate("build")
        val plugins = build.childOrCreate("plugins", BUILD_ELEMENT_ORDER)
        // 幂等：`pom.withXml` 可能被执行多次，先清掉自己上次写的那个 plugin
        plugins.removePlugins("spring-boot-maven-plugin")
        val plugin = Node(plugins, "plugin")
        Node(plugin, "groupId", "org.springframework.boot")
        Node(plugin, "artifactId", "spring-boot-maven-plugin")
        Node(plugin, "version", springBootVersion)

        val configuration = Node(plugin, "configuration")
        Node(configuration, "fork", "true")

        val executions = Node(plugin, "executions")
        val execution = Node(executions, "execution")
        Node(Node(execution, "goals"), "goal", "repackage")

        builder.setLength(0)
        builder.append(root.toPomString())

    }
}

/**
 * Maven 侧「继承载荷」。
 *
 * 这些聚合/父 pom 对外的用途就是被业务系统当作 `<parent>` 继承：Maven 的继承是"内容级"的，
 * 而 Gradle 产出的 pom 不带继承链（也不该带），所以把迁移前 Maven pom 的**有效语义**直接写进产出 pom。
 *
 * 注意：这是给 Maven 消费者用的形态（provided/test 作用域、pluginManagement 等 Gradle 模型无法表达），
 * Gradle 消费者侧用 `platform(...)` / 项目依赖即可，两套并行、互不影响。
 */
class MavenParentContent(
    /** `<dependencyManagement>` 里 import 的 BOM（`g:a:v`），null 表示不写 */
    val bomImport: String? = null,
    /** 以 `optional` 表达的依赖（`g:a` 或 `g:a:v`）：随父 pom 继承给业务系统，但不向其传递 */
    val providedDependencies: List<String> = emptyList(),
    /** test 作用域依赖 */
    val testDependencies: List<String> = emptyList(),
    /** maven-compiler-plugin 的 `<source>` / `<target>` */
    val compilerJavaVersion: String,
    /** maven-compiler-plugin 的 `<parameters>true</parameters>` */
    val compilerParameters: Boolean = false,
    /** maven-compiler-plugin 的 `<annotationProcessorPaths>`（`g:a:v`） */
    val annotationProcessorPaths: List<String> = emptyList(),
    /** pluginManagement 中 spring-boot-maven-plugin 的版本，null 表示不写 */
    val springBootPluginVersion: String? = null
)

/** `<project>` 直接子元素的顺序（Maven 4.0.0 模型），插入时按此排序 */
private val POM_ELEMENT_ORDER = listOf(
    "modelVersion", "parent", "groupId", "artifactId", "version", "packaging", "name", "description", "url",
    "inceptionYear", "organization", "licenses", "developers", "contributors", "mailingLists", "prerequisites",
    "modules", "scm", "issueManagement", "ciManagement", "distributionManagement", "properties",
    "dependencyManagement", "dependencies", "repositories", "pluginRepositories", "build", "reporting", "profiles"
)

/** `<build>` 直接子元素的顺序（pluginManagement 必须在 plugins 之前） */
private val BUILD_ELEMENT_ORDER = listOf(
    "sourceDirectory", "scriptSourceDirectory", "testSourceDirectory", "outputDirectory", "testOutputDirectory",
    "defaultGoal", "resources", "testResources", "directory", "finalName", "filters", "pluginManagement", "plugins"
)

/** 删除同名直接子节点 */
private fun Node.removeChildren(name: String) {
    (children() as MutableList<Any>).removeAll { it is Node && name == it.name().toString() }
}

/** 删除 `<plugins>` 下指定 artifactId 的 plugin 节点（注入幂等用） */
private fun Node.removePlugins(artifactId: String) {
    (children() as MutableList<Any>).removeAll {
        it is Node && "plugin" == it.name().toString() && artifactId == it.child("artifactId")?.text()
    }
}

/** 取同名子节点（可能不存在） */
private fun Node.child(name: String): Node? =
    children().filterIsInstance<Node>().firstOrNull { name == it.name().toString() }

/** 取同名子节点，没有则按 Maven 模型顺序新建 */
private fun Node.childOrCreate(name: String, order: List<String> = POM_ELEMENT_ORDER): Node {

    child(name)?.let { return it }

    // 注意：`Node(parent, name)` 构造时**已经**把节点挂到父节点末尾，不能再手动 add（会重复）
    val node = Node(this, name)
    val children = children() as MutableList<Any>
    val position = order.indexOf(name).let { if (it < 0) order.size else it }
    val index = children.indexOfFirst {
        it is Node && (order.indexOf(it.name().toString()).let { i -> if (i < 0) order.size else i }) > position
    }
    if (index >= 0 && index < children.size - 1) {
        children.remove(node)
        children.add(index, node)
    }
    return node
}

/** `g:a[:v]` 解析 */
private fun String.toGav(): Triple<String, String, String?> {
    val parts = split(':')
    return Triple(parts[0], parts[1], parts.getOrNull(2))
}

/** 追加一个 `<dependency>`（带作用域），用于让 Maven 消费者从父 pom 继承该依赖 */
private fun Node.addDependency(gav: String, scope: String) {

    val (groupId, artifactId, version) = gav.toGav()
    val dependency = Node(this, "dependency")
    Node(dependency, "groupId", groupId)
    Node(dependency, "artifactId", artifactId)
    if (!version.isNullOrEmpty()) {
        Node(dependency, "version", version)
    }
    Node(dependency, "scope", scope)
}

/**
 * 追加一个以 Maven `optional` 表达的 `<dependency>`：不写 `<scope>`（默认 compile），只标 `<optional>true</optional>`。
 *
 * 语义：编译期可用、**不向下游传递**（与 `provided` 在传递性上等价），但表达的是"可选用能力"而非
 * "容器提供"——使用方据此知道本模块支持哪些可选能力，且能按需引入。
 */
private fun Node.addOptionalDependency(gav: String) {

    val (groupId, artifactId, version) = gav.toGav()
    val dependency = Node(this, "dependency")
    Node(dependency, "groupId", groupId)
    Node(dependency, "artifactId", artifactId)
    if (!version.isNullOrEmpty()) {
        Node(dependency, "version", version)
    }
    Node(dependency, "optional", "true")
}

/**
 * 把 Gradle `compileOnly` 依赖以 Maven `optional` 写进产出 pom（详见 [addOptionalDependency]）。
 *
 * 背景：`compileOnly` 不进 Gradle 的发布元数据，迁移前 Maven 侧用 `provided` / `optional` 表达这些依赖；
 * 按用户要求统一改用 `optional`，使使用方从 pom 就能看到可选用能力与其解析后的版本。
 *
 * 幂等与边界：
 * - `pom.withXml` 可能被执行多次，已存在同 `g:a` 条目即跳过——不改动 Gradle 自己写的 compile/runtime 条目，
 *   避免把传递依赖误标成 optional。
 * - 坐标为 `g:a:v` 形态的纯字符串（由调用方在配置期取好），本函数不接触 Project。
 */
fun MavenPublication.cAddOptionalDependencies(coordinates: List<String>) {

    pom.withXml {

        val builder = asString()
        val root = builder.toString().toPomNode()
        val dependencies = root.childOrCreate("dependencies")

        val existing = dependencies.children()
            .filterIsInstance<Node>()
            .filter { "dependency" == it.name().toString() }
            .mapNotNull { node ->
                val groupId = node.child("groupId")?.text()
                val artifactId = node.child("artifactId")?.text()
                if (null == groupId || null == artifactId) null else "$groupId:$artifactId"
            }
            .toSet()

        coordinates.forEach { gav ->
            val (groupId, artifactId) = gav.toGav()
            if ("$groupId:$artifactId" in existing) return@forEach
            dependencies.addOptionalDependency(gav)
        }

        builder.setLength(0)
        builder.append(root.toPomString())

    }
}

/**
 * 把 Maven 父 pom 的继承载荷写进产出 pom（详见 [MavenParentContent]）。
 */
fun MavenPublication.cAddInheritedMavenContent(content: MavenParentContent) {

    pom.withXml {

        val builder = asString()
        val root = builder.toString().toPomNode()

        // 幂等：`pom.withXml` 可能被执行多次，先清掉自己上次写的内容（这些 pom 本来就没有对应节点）
        root.removeChildren("dependencyManagement")
        root.removeChildren("dependencies")
        val build = root.childOrCreate("build")
        build.removeChildren("pluginManagement")
        val plugins = build.childOrCreate("plugins", BUILD_ELEMENT_ORDER)
        plugins.removePlugins("maven-compiler-plugin")

        // ---- dependencyManagement：import 版本管理 BOM ----
        if (!content.bomImport.isNullOrEmpty()) {
            val (groupId, artifactId, version) = content.bomImport.toGav()
            val dependencies = root.childOrCreate("dependencyManagement").childOrCreate("dependencies")
            val dependency = Node(dependencies, "dependency")
            Node(dependency, "groupId", groupId)
            Node(dependency, "artifactId", artifactId)
            if (!version.isNullOrEmpty()) {
                Node(dependency, "version", version)
            }
            Node(dependency, "type", "pom")
            Node(dependency, "scope", "import")
        }

        // ---- dependencies：随父 pom 继承给业务系统的依赖（provided / test）----
        if (content.providedDependencies.isNotEmpty() || content.testDependencies.isNotEmpty()) {
            val dependencies = root.childOrCreate("dependencies")
            content.providedDependencies.forEach { dependencies.addOptionalDependency(it) }
            content.testDependencies.forEach { dependencies.addDependency(it, "test") }
        }

        // ---- build/plugins：编译器配置（source/target/encoding/parameters/注解处理器）----
        val compiler = Node(plugins, "plugin")
        Node(compiler, "groupId", "org.apache.maven.plugins")
        Node(compiler, "artifactId", "maven-compiler-plugin")
        val configuration = Node(compiler, "configuration")
        Node(configuration, "source", content.compilerJavaVersion)
        Node(configuration, "target", content.compilerJavaVersion)
        Node(configuration, "encoding", "UTF-8")
        if (content.compilerParameters) {
            Node(configuration, "parameters", "true")
        }
        if (content.annotationProcessorPaths.isNotEmpty()) {
            val paths = Node(configuration, "annotationProcessorPaths")
            content.annotationProcessorPaths.forEach { gav ->
                val (groupId, artifactId, version) = gav.toGav()
                val path = Node(paths, "path")
                Node(path, "groupId", groupId)
                Node(path, "artifactId", artifactId)
                if (!version.isNullOrEmpty()) {
                    Node(path, "version", version)
                }
            }
        }

        // ---- build/pluginManagement：spring-boot-maven-plugin 版本与 repackage 执行 ----
        if (!content.springBootPluginVersion.isNullOrEmpty()) {
            val management = build.childOrCreate("pluginManagement", BUILD_ELEMENT_ORDER)
            val managedPlugins = management.childOrCreate("plugins", BUILD_ELEMENT_ORDER)
            val plugin = Node(managedPlugins, "plugin")
            Node(plugin, "groupId", "org.springframework.boot")
            Node(plugin, "artifactId", "spring-boot-maven-plugin")
            Node(plugin, "version", content.springBootPluginVersion)
            val pluginConfiguration = Node(plugin, "configuration")
            Node(pluginConfiguration, "fork", "true")
            val executions = Node(plugin, "executions")
            val execution = Node(executions, "execution")
            Node(Node(execution, "goals"), "goal", "repackage")
        }

        builder.setLength(0)
        builder.append(root.toPomString())

    }
}
