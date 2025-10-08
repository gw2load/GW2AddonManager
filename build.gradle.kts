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
import com.osmerion.gradle.jdk.tools.tasks.JLink
import com.osmerion.jvm.launcher.gradle.tasks.BuildJvmLauncher
import com.osmerion.jvm.launcher.gradle.tasks.GenerateLauncherConfig

plugins {
    alias(buildDeps.plugins.gradle.buildconfig)
    alias(buildDeps.plugins.gradle.jdkTools)
    alias(buildDeps.plugins.jetbrainsCompose)
    alias(buildDeps.plugins.jvmLauncher)
    alias(buildDeps.plugins.kotlin.multiplatform)
    alias(buildDeps.plugins.kotlin.plugin.compose)
    alias(buildDeps.plugins.kotlin.plugin.serialization)
}

jvmLauncher {
    launchers {
        register("GW2AddonManager") {
            fileVersion(1, 0, 0, 0)
            productVersion(1, 0, 0, 0)

            stringFileInfo {
                companyName = "GW2ToolBelt"
                fileDescription = "Guild Wars 2 Add-on Manager"
                fileVersion = "$version"
                internalName = "GW2AddOnManager"
                productName = "Guild Wars 2 Add-on Manager"
                productVersion = "$version"
            }

            icon = layout.projectDirectory.file("icon.ico")

            mainClassName = "com/gw2tb/manager/MainKt"
            libjvmPath = "./runtime/bin/server/jvm.dll"

            jvmArgs.add("--enable-native-access=ALL-UNNAMED")

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
        languageVersion = JavaLanguageVersion.of(25)
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.components.resources)
                implementation(compose.desktop.currentOs)
                implementation(compose.foundation)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(buildDeps.compose.constraintlayout)
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
    buildConfigField("MANIFEST_URL", providers.gradleProperty("com.gw2tb.manager.manifest-url"))
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

        jvmArgs("--enable-native-access=ALL-UNNAMED")

        mainClass =  "com.gw2tb.manager.MainKt"
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }

    val jlink = register<JLink>("jlink") {
        destinationDirectory = layout.buildDirectory.dir("jlink")

        addModules.addAll(
            "java.desktop",
            "java.management",
            "java.net.http",
            "jdk.unsupported"
        )

        args.addAll(
            "--compress", "zip-6",
            "--disable-plugin", "release-info",
            "--vm=server"
        )
    }

    val compileGW2AddonManagerJvmLauncher by getting(BuildJvmLauncher::class)
    val generateGW2AddonManagerLauncherConfig by getting(GenerateLauncherConfig::class)

    val copyBundle = register<Sync>("copyBundle") {
        dependsOn(compileGW2AddonManagerJvmLauncher, generateGW2AddonManagerLauncherConfig, jlink)

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

        into(".") {
            from(compileGW2AddonManagerJvmLauncher.destinationDirectory.file("GW2AddOnManager.exe"))
            from(compileGW2AddonManagerJvmLauncher.destinationDirectory.file("config.toml"))
            from(generateGW2AddonManagerLauncherConfig.outputFile)
        }
    }

    register<Zip>("bundle") {
        dependsOn(copyBundle)

        destinationDirectory = layout.buildDirectory.dir("bundles")

        from(copyBundle.get().destinationDir)
    }
}
