plugins {
    alias(buildDeps.plugins.kotlin.multiplatform)
    alias(buildDeps.plugins.kotlin.plugin.compose)
    alias(buildDeps.plugins.kotlin.plugin.serialization)
    alias(buildDeps.plugins.jetbrainsCompose)
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
                implementation(buildDeps.decompose)
                implementation(buildDeps.decompose.extensions.compose)
                implementation(buildDeps.essenty.lifecycle.coroutines)
                implementation(buildDeps.kotlinx.serialization.json)
                implementation(buildDeps.ktor.client.core)
                implementation(buildDeps.ktor.client.logging)
            }
        }

        jvmMain {
            dependencies {
                implementation(buildDeps.slf4j.api)
                implementation(buildDeps.log4j.core)
                implementation(buildDeps.log4j.slf4j2.impl)
                implementation(buildDeps.jackson.databind)

                runtimeOnly(buildDeps.ktor.client.java)
            }
        }

        jvmTest {
            dependencies {
                implementation(buildDeps.junit.jupiter.api)
                implementation(buildDeps.mockito.core)

                runtimeOnly(buildDeps.junit.jupiter.engine)
            }
        }
    }
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

repositories {
    mavenCentral()
    google()
}