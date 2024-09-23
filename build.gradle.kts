import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(buildDeps.plugins.kotlin.multiplatform)
    alias(buildDeps.plugins.kotlin.plugin.compose)
    alias(buildDeps.plugins.kotlin.plugin.serialization)
    alias(buildDeps.plugins.jetbrainsCompose)
}

compose {
    desktop {
        application {
            javaHome = javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(22)) }.get().executablePath.asFile.parentFile.parent
            mainClass = "com.gw2tb.manager.MainKt"

            nativeDistributions {
                modules("java.net.http")

                targetFormats(TargetFormat.Exe, TargetFormat.Msi)

                windows {
                    dirChooser = true
                    menu = true
                }
            }
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.components.resources)
                implementation(compose.desktop.currentOs)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(buildDeps.decompose)
                implementation(buildDeps.decompose.extensions.compose)
                implementation(buildDeps.essenty.lifecycle.coroutines)
                implementation(buildDeps.filekit.compose)
                implementation(buildDeps.kotlinx.coroutines.slf4j)
                implementation(buildDeps.kotlinx.serialization.json)
                implementation(buildDeps.ktor.client.core)
                implementation(buildDeps.ktor.client.logging)
            }
        }

        jvmMain {
            dependencies {
                implementation(buildDeps.caffeine)
                implementation(buildDeps.jackson.databind)
                implementation(buildDeps.log4j.core)
                implementation(buildDeps.log4j.slf4j2.impl)
                implementation(buildDeps.slf4j.api)

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
    withType<Jar>().configureEach {
        manifest {
            attributes(
                "Implementation-Title" to "com.gw2tb.manager",
                "Implementation-Vendor" to "com.gw2tb",
                "Implementation-Version" to project.version
            )
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}