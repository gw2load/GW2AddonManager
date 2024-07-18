plugins {
    alias(buildDeps.plugins.kotlin.multiplatform)
    alias(buildDeps.plugins.kotlin.plugin.compose)
    alias(buildDeps.plugins.kotlin.plugin.serialization)
    alias(buildDeps.plugins.jetbrainsCompose)
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
                implementation(buildDeps.decompose)
                implementation(buildDeps.decompose.extensions.compose)
                implementation(buildDeps.essenty.lifecycle.coroutines)
                implementation(buildDeps.kotlinx.serialization.json)
                implementation(buildDeps.ktor.client.core)
            }
        }

        jvmMain {
            dependencies {
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