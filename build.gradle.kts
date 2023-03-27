import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val coroutine_version: String by project

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.darwin.dev"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
                implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
                implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
                implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
                implementation("io.ktor:ktor-server-html-builder:$ktor_version")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-pre.517")
                implementation("ch.qos.logback:logback-classic:$logback_version")
                implementation("com.soywiz.korlibs.kds:kds:4.0.0-alpha-6")
                implementation("com.lordcodes.turtle:turtle:0.8.0")
            }
            resources.srcDirs("src/jvmMain/resources")
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "host-ISO-8583-simulator"
            packageVersion = "1.0.0"
        }
    }
}
