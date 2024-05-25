plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.jetbrainsCompose)
}

composeCompiler {
    enableStrongSkippingMode = true
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
            }
        }

        jvmMain {
            dependencies {
                runtimeOnly(libs.ktor.client.java)
            }
        }
    }
}

repositories {
    mavenCentral()
    google()
}