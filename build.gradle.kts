import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")

    // apply kotlinx serialization plugin
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.31"
}

group = "com.realityexpander"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)

                implementation("io.ktor:ktor-client-core:1.6.8")
                implementation("io.ktor:ktor-client-cio-jvm:1.6.8")
                implementation("io.ktor:ktor-client-okhttp:1.6.8")
                implementation("io.ktor:ktor-client-serialization:1.6.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.2.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "app.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "StopwatchComposeDesktop"
            packageVersion = "1.0.0"
            version = "0.1-SNAPSHOT"
            description = "Compose Example App"
            copyright = "© 2020 My Name. All rights reserved."
            vendor = "Example vendor"
//            licenseFile.set(project.file("LICENSE.txt"))

            macOS {
                iconFile.set(project.file("src/jvmMain/resources/stopwatch.icns"))
                dockName = "Stopwatch"
            }
        }
    }
}
