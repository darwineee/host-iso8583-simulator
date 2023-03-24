pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("multiplatform").version(extra["kotlin_version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
        id("io.ktor.plugin").version(extra["ktor_version"] as String)
        id("org.jetbrains.kotlin.plugin.serialization").version(extra["serialization_version"] as String)
    }
}

rootProject.name = "host-iso8583-simulator"

