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
package com.gw2tb.manager.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GenerateLauncherConfig : DefaultTask() {

    @get:InputFiles
    @get:Classpath
    abstract val classpath: ConfigurableFileCollection

    @get:Input
    abstract val classpathRoot: Property<String>

    @get:Input
    abstract val jvmArgs: ListProperty<String>

    @get:Input
    abstract val libjvmPath: Property<String>

    @get:Input
    abstract val mainClassName: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    protected fun generate() {
        val classpath = classpath.files
        val classpathRoot = classpathRoot.get()

        val jvmArgs = buildList {
            addAll(jvmArgs.get())

            if (classpath.isNotEmpty()) {
                add("-Djava.class.path=${classpath.joinToString(separator = ";") { "$classpathRoot/${it.name}" }}")
            }
        }

        val libjvmPath = libjvmPath.get()
        val mainClassName = mainClassName.get()

        val outputFile = outputFile.get().asFile

        // Behold! The best TOML "serializer" you'll ever encounter!
        val content = buildString {
            appendLine("libjvm_path = \"$libjvmPath\"")
            appendLine("main_class = \"$mainClassName\"")

            append("jvm_args = [")

            val jvmArgsString = jvmArgs.joinToString(separator = ",\n") { "\"$it\"" }
            if (jvmArgsString.isNotEmpty()) {
                append("\n")
                append(jvmArgsString.prependIndent(indent = "    "))
                append("\n")
            }

            append("]")
        }

        outputFile.writeText(content)
    }

}