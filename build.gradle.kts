/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024 Leon Linhart
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU Lesser General Public License as published
 * by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
import com.gw2tb.manager.build.GenerateLauncherConfig
import com.gw2tb.manager.build.JLink
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    alias(buildDeps.plugins.kotlin.multiplatform)
    alias(buildDeps.plugins.kotlin.plugin.compose)
    alias(buildDeps.plugins.kotlin.plugin.serialization)
    alias(buildDeps.plugins.jetbrainsCompose)
    id("dummy")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(23)
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

        commonTest {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(buildDeps.turbine)
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

    register<JavaExec>("run") {
        dependsOn("jvmJar")

        workingDir = mkdir(project.layout.projectDirectory.dir("run").asFile)

        javaLauncher = project.javaToolchains.launcherFor(project.java.toolchain)

        classpath(project.tasks["jvmJar"])
        classpath(configurations["jvmRuntimeClasspath"])

        mainClass =  "com.gw2tb.manager.MainKt"
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }

    val jlink = register<JLink>("jlink") {
        val toolchain = project.extensions.getByType<JavaPluginExtension>().toolchain
        val service = project.extensions.getByType<JavaToolchainService>()

        executable.set(layout.file(service.compilerFor(toolchain).map {
            it.executablePath.asFile.resolveSibling("jlink${if (Os.isFamily(Os.FAMILY_WINDOWS)) ".exe" else ""}")
        }))

        destinationDir.set(layout.buildDirectory.dir("jlink"))

        addModules.addAll(
            "java.desktop",
            "java.management",
            "java.net.http",
            "jdk.unsupported"
        )
    }

    val generateLauncherConfig = register<GenerateLauncherConfig>("generateLauncherConfig") {
        outputFile = layout.buildDirectory.file("tmp/$name/config.toml")

        mainClassName = "com/gw2tb/manager/MainKt"
        libjvmPath = "./runtime/bin/server/jvm.dll"

        jvmArgs.add("--enable-native-access=ALL-UNNAMED")

        classpathRoot = "./jars"
        classpath.from(project.tasks["jvmJar"])
        classpath.from(configurations["jvmRuntimeClasspath"])
    }

    val copyBundle = register<Copy>("copyBundle") {
        dependsOn(generateLauncherConfig, jlink)

        destinationDir = layout.buildDirectory.dir("tmp/bundle").get().asFile

        into("jars") {
            from(project.tasks["jvmJar"])
            from(configurations["jvmRuntimeClasspath"])
        }

        into("runtime") {
            from(jlink.get().destinationDir)
        }

        into(".") {
            from(file("launcher/target/release/GW2AddOnManager.exe"))
            from(generateLauncherConfig.get().outputFile)
        }
    }

    register<Zip>("bundle") {
        dependsOn(copyBundle)

        destinationDirectory = layout.buildDirectory.dir("bundles")

        from(copyBundle.get().destinationDir)
    }
}