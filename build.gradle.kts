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
import com.gw2tb.manager.build.tasks.GenerateLauncherApplicationManifest
import com.gw2tb.manager.deploy.tasks.DownloadGw2Load
import com.gw2tb.manager.deploy.tasks.UpdateManagerManifests
import com.osmerion.gradle.jdk.tools.tasks.JLink
import com.osmerion.jvm.launcher.gradle.VersionNumber
import com.osmerion.jvm.launcher.gradle.tasks.BuildJvmLauncher
import com.osmerion.jvm.launcher.gradle.tasks.GenerateLauncherConfig

plugins {
    alias(buildDeps.plugins.cyclonedx)
    alias(buildDeps.plugins.gradle.buildconfig)
    alias(buildDeps.plugins.gradle.jdkTools)
    alias(buildDeps.plugins.jetbrainsCompose)
    alias(buildDeps.plugins.jvmLauncher)
    alias(buildDeps.plugins.licensee)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("com.gw2tb.manager.build.dummy")
    id("com.gw2tb.manager.deploy.dummy")
}

jvmLauncher {
    launchers {
        register(project.name) {
            fun String.toVersionNumber(): VersionNumber {
                val segments = this.split(".")
                require(segments.size == 4) { "Version number must have four segments: $this" }
                return VersionNumber(segments[0].toShort(), segments[1].toShort(), segments[2].toShort(), segments[3].toShort())
            }

            fileVersion.set(
                providers.environmentVariable("MS_VERSION")
                    .map(String::toVersionNumber)
                    .orElse(VersionNumber(0, 0, 0, 0))
            )

            productVersion.set(
                providers.environmentVariable("MS_VERSION")
                    .map(String::toVersionNumber)
                    .orElse(VersionNumber(0, 0, 0, 0))
            )

            stringFileInfo {
                companyName = "GW2ToolBelt"
                fileDescription = "Guild Wars 2 Add-on Manager"
                fileVersion = "$version"
                internalName = project.name
                productName = "Guild Wars 2 Add-on Manager"
                productVersion = "$version"
            }

            icon = layout.projectDirectory.file("icon.ico")

            mainClassName = "com/gw2tb/manager/MainKt"
            libjvmPath = "./runtime/bin/server/jvm.dll"

            jvmArgs.add("--enable-native-access=ALL-UNNAMED")
            jvmArgs.add("-Dmanager.dir=${path(".")}")

            classpath.add(provider { "./app.jar" })
            classpath.addAll(provider {
                val runtimeClasspath = project.configurations.getByName("jvmRuntimeClasspath")

                runtimeClasspath.resolvedConfiguration
                    .resolvedArtifacts
                    .map { artifact ->
                        val group = artifact.moduleVersion.id.group.replace('.', '/')
                        "./libs/$group/${artifact.file.name}"
                    }
            })
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(buildDeps.compose.components.resources)
                implementation(buildDeps.compose.foundation)
                implementation(buildDeps.compose.material.iconsExtended)
                implementation(buildDeps.compose.constraintlayout)
                implementation(buildDeps.compose.ui)
                implementation(buildDeps.decompose)
                implementation(buildDeps.decompose.extensions.compose)
                implementation(buildDeps.essenty.lifecycle.coroutines)
                implementation(buildDeps.filekit.compose)
                implementation(buildDeps.kotlinx.coroutines.slf4j)
                implementation(buildDeps.ktor.client.core)
                implementation(buildDeps.ktor.client.logging)
                implementation(buildDeps.semver)
                implementation(libs.kotlinx.serialization.json)
                implementation("com.gw2tb.manager:addon-manifest-lib:0.1.0")
                implementation("com.gw2tb.manager:fileinfo-reader:0.1.0")
                implementation("com.gw2tb.manager:manager-manifest-lib:0.1.0")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(buildDeps.compose.ui.test)
                implementation(buildDeps.ktor.client.mock)
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

                runtimeOnly(buildDeps.kotlinx.coroutines.swing)
                runtimeOnly(buildDeps.ktor.client.java)
            }
        }

        jvmTest {
            dependencies {
                implementation(project.dependencies.platform(buildDeps.junit.bom))
                implementation(buildDeps.junit.jupiter.api)
                implementation(buildDeps.mockito.core)

                runtimeOnly(buildDeps.junit.jupiter.engine)
                runtimeOnly(buildDeps.junit.platform.launcher)
            }
        }
    }
}

buildConfig {
    packageName = "com.gw2tb.manager.internal"

    buildConfigField("BUILD_VERSION", provider { "${project.version}" })
    buildConfigField("DISCORD_URL", providers.gradleProperty("com.gw2tb.manager.discord-url"))
    buildConfigField("GITHUB_URL", providers.gradleProperty("com.gw2tb.manager.github-url"))
    buildConfigField("HELP_URL", providers.gradleProperty("com.gw2tb.manager.help-url"))
    buildConfigField("ISSUES_URL", providers.gradleProperty("com.gw2tb.manager.issues-url"))
    buildConfigField("ADDON_MANIFEST_URL", providers.gradleProperty("com.gw2tb.manager.addon-manifest-url"))
    buildConfigField("MANAGER_MANIFEST_BASE_URL", providers.gradleProperty("com.gw2tb.manager.manager-manifest-base-url"))
}

licensee {
    allow("Apache-2.0")

    allowUrl("https://github.com/hypfvieh/dbus-java/blob/master/LICENSE") // MIT
    allowUrl("https://github.com/vinceglb/FileKit/blob/main/LICENSE") // MIT
    allowUrl("https://raw.githubusercontent.com/z4kn4fein/kotlin-semver/main/LICENSE") // MIT
    allowUrl("https://opensource.org/license/mit") // MIT
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

    val downloadGw2Load = register<DownloadGw2Load>("downloadGw2Load") {
        manifestUrl = providers.gradleProperty("com.gw2tb.manager.addon-manifest-url")
        destination = project.layout.buildDirectory.file("tmp/run/loader/msimg32.dll")
    }

    register<JavaExec>("run") {
        dependsOn(downloadGw2Load, "jvmJar")

        workingDir = mkdir(project.layout.buildDirectory.dir("tmp/run").get().asFile)

        javaLauncher = project.javaToolchains.launcherFor(project.java.toolchain)

        classpath(project.tasks["jvmJar"])
        classpath(configurations["jvmRuntimeClasspath"])

        jvmArgs("--enable-native-access=ALL-UNNAMED")
        jvmArgs("-Dmanager.dir=${project.layout.buildDirectory.dir("tmp/run").get().asFile.absolutePath}")

        mainClass =  "com.gw2tb.manager.MainKt"
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }

    val jlink = register<JLink>("jlink") {
        destinationDirectory = layout.buildDirectory.dir("jlink")

        addModules.addAll(
            "java.compiler",
            "java.desktop",
            "java.instrument",
            "java.logging",
            "java.management",
            "java.net.http",
            "java.rmi",
            "java.scripting",
            "java.sql",
            "jdk.crypto.ec",
            "jdk.security.auth",
            "jdk.unsupported"
        )

        args.addAll(
            "--compress", "zip-6",
            "--disable-plugin", "release-info",
            "--vm=server"
        )
    }

    val generateGW2AddonManagerLauncherConfig by getting(GenerateLauncherConfig::class)

    val generateGW2AddOnManagerLauncherManifest by registering(GenerateLauncherApplicationManifest::class) {
        assemblyName = project.name
        version = jvmLauncher.launchers.named(project.name).flatMap { it.fileVersion }.map { it.toString(".") }
    }

    val compileGW2AddonManagerJvmLauncher by getting(BuildJvmLauncher::class) {
        dependsOn(generateGW2AddOnManagerLauncherManifest)

        resources.from(generateGW2AddOnManagerLauncherManifest.map { it.destinationDirectory.file("manifest.rc") })
    }

    val copyBundle = register<Sync>("copyBundle") {
        dependsOn(compileGW2AddonManagerJvmLauncher, downloadGw2Load, generateGW2AddonManagerLauncherConfig, jlink)

        destinationDir = layout.buildDirectory.dir("tmp/bundle").get().asFile

        val runtimeClasspath = project.configurations.getByName("jvmRuntimeClasspath")

        runtimeClasspath.resolvedConfiguration
            .resolvedArtifacts
            .forEach { artifact ->
                from(artifact.file) {
                    val group = artifact.moduleVersion.id.group.replace('.', '/')
                    into("libs/$group")
                }
            }

        from(jlink.flatMap(JLink::destinationDirectory)) {
            into("runtime")
        }

        from(this@tasks.named("jvmJar")) {
            rename("(.*)", "app.jar")
        }

        from(downloadGw2Load.flatMap(DownloadGw2Load::destination)) {
            into("loader")
        }

        into(".") {
            from(compileGW2AddonManagerJvmLauncher.destinationDirectory.file("${project.name}.exe"))
            from(compileGW2AddonManagerJvmLauncher.destinationDirectory.file("config.toml"))
            from(generateGW2AddonManagerLauncherConfig.outputFile)
            from(cyclonedxBom.get().jsonOutput)
        }
    }

    register<Zip>("bundle") {
        dependsOn(copyBundle)

        destinationDirectory = layout.buildDirectory.dir("bundles")

        from(copyBundle.get().destinationDir)
    }

    register<UpdateManagerManifests>("updateManifests") {
        fun String.toVersionNumber(): List<UShort> {
            val segments = this.split(".")
            require(segments.size == 4) { "Version number must have four segments" }
            return segments.map(String::toUShort)
        }

        version.set(
            providers.environmentVariable("MS_VERSION")
                .map(String::toVersionNumber)
                .orElse(listOf(0u, 0u, 0u, 0u))
        )

        versionString.set(
            providers.environmentVariable("VERSION")
                .orElse("${project.version}")
        )
    }
}
