package com.c332030.ctool4j.gradle.buildsrc.util

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler

/**
 * <p>
 * Description: CGradleRepositoriesUtils
 * </p>
 *
 * 仓库配置：地址与凭据一律取自环境变量（MAVEN_CENTRAL / NEXUS_*），不写死地址。
 * 次序：snapshot → release → central。
 *
 * @author c332030
 * @since 2026/9/26
 */

/**
 * 依赖解析仓库：snapshot → release → central，一律走远端——**不配置 mavenLocal**。
 *
 * mavenLocal 会把 `~/.m2/repository` 纳入解析：本机 `mvn install` 的产物（含同名同版本的旧快照）会被就近取用，
 * 同一个提交在不同机器上解析到不同构件，**构建不可重现**；缺构件时还会静默回退到本地那份，掩盖远端源的问题。
 * 故本项目只认远端源，本地已装构件不参与构建。
 *
 * 需要以仓库坐标取本项目自身的构件时，改由 snapshot 源提供（`NEXUS_SNAPSHOT_URL` 本就在次序内）；
 * 本仓的模块间依赖走 Gradle 项目依赖，不受本取舍影响。
 */
fun RepositoryHandler.configureSharedRepositories(project: Project) {

    val nexusUsername = project.getConfigValue("NEXUS_USERNAME")
    val nexusPassword = project.getConfigValue("NEXUS_PASSWORD")

    val nexusSnapshotUrl = project.getConfigValue("NEXUS_SNAPSHOT_URL")
    if (!nexusSnapshotUrl.isNullOrEmpty()) {
        maven {
            url = project.uri(nexusSnapshotUrl)
            credentials {
                username = nexusUsername
                password = nexusPassword
            }
        }
    }

    val nexusReleaseUrl = project.getConfigValue("NEXUS_RELEASE_URL")
    if (!nexusReleaseUrl.isNullOrEmpty()) {
        maven {
            url = project.uri(nexusReleaseUrl)
            credentials {
                username = nexusUsername
                password = nexusPassword
            }
        }
    }

    val mavenCentralUrl = project.getConfigValue("MAVEN_CENTRAL")
    if (!mavenCentralUrl.isNullOrEmpty()) {
        maven {
            url = project.uri(mavenCentralUrl)
        }
    } else {
        mavenCentral()
    }

}
