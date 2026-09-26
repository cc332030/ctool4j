
plugins {

    `kotlin-dsl`

}

fun getConfigValue(key: String): String? {
    return providers.systemProperty(key)
        .orElse(providers.environmentVariable(key))
        .orElse(providers.gradleProperty(key))
        .getOrNull()
}

val mavenCentralUrl = getConfigValue("MAVEN_CENTRAL")

repositories {

    if (!mavenCentralUrl.isNullOrEmpty()) {
        maven {
            url = uri(mavenCentralUrl)
        }
    }

    gradlePluginPortal()
    mavenCentral()

}
