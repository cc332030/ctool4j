package com.c332030.ctool4j.gradle.buildsrc.util

import groovy.util.Node
import groovy.xml.XmlNodePrinter
import groovy.xml.XmlParser
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * <p>
 * Description: CGradlePublishUtils
 * </p>
 *
 * 发布相关工具：把 Maven 的父 pom 继承链与 boot-parent 的插件配置复刻到 Gradle 产出的 pom 上。
 *
 * 说明：
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
 * 推断本模块在 Maven 体系中的父构件：
 * 由目录结构推导——向上取"最近一个本身也是模块（含 build.gradle.kts）的祖先目录"，
 * 取不到则落到根构件（rootProject.name）。
 *
 * 该推导与迁移前的 `<parent>` 链逐模块一致（模块目录层级即 Maven 继承层级）。
 */
fun Project.cParentArtifactId(): String {

    val rootPath = rootDir.invariantSeparatorsPath
    var dir: File? = projectDir.parentFile

    while (dir != null && dir.invariantSeparatorsPath != rootPath) {
        if (File(dir, "build.gradle.kts").exists()) {
            return dir.name
        }
        dir = dir.parentFile
    }

    return rootProject.name
}

/**
 * 往产出 pom 里注入 `<parent>`（Maven 继承链）。
 *
 * Gradle 生成 pom 时不写 `<parent>`；不注入则外部 Maven 侧"以 ctool4j 的 pom 为 parent"
 * 的用法断链（例如 `ctool4j-boot-parent`）。
 */
fun MavenPublication.cAddParentPom(groupId: String, artifactId: String, version: String) {

    pom.withXml {

        // XmlProvider#asString() 返回其内部 StringBuilder，就地改写即可生效（无 setter 可用）
        val builder = asString()
        val root = builder.toString().toPomNode()

        val parent = Node(root, "parent")
        Node(parent, "groupId", groupId)
        Node(parent, "artifactId", artifactId)
        Node(parent, "version", version)

        // <parent> 必须排在 <modelVersion> 之后
        val children = root.children() as MutableList<Any>
        children.remove(parent)
        children.add(1, parent)

        builder.setLength(0)
        builder.append(root.toPomString())

    }
}

/**
 * 设置产出 pom 的 `<packaging>`（无组件的聚合/父 pom 用 `pom`）。
 *
 * 走 XML 改写而非 `MavenPom#getPackaging()`：后者在 Kotlin DSL 下取到的并非 `Property<String>`。
 */
fun MavenPublication.cSetPackaging(packaging: String) {

    pom.withXml {

        val builder = asString()
        val root = builder.toString().toPomNode()

        val children = root.children() as MutableList<Any>
        children.removeAll { it is Node && "packaging" == it.name() }
        Node(root, "packaging", packaging)

        builder.setLength(0)
        builder.append(root.toPomString())

    }
}

/**
 * 往产出 pom 里注入 `<build><plugins>`（当前仅 `ctool4j-boot-parent` 需要）：
 * 该构件对外是业务应用的 Maven 父 pom，须带 spring-boot-maven-plugin 的版本与 repackage 执行。
 */
fun MavenPublication.cAddSpringBootPlugin(springBootVersion: String) {

    pom.withXml {

        val builder = asString()
        val root = builder.toString().toPomNode()
        val build = Node(root, "build")
        val plugins = Node(build, "plugins")
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
