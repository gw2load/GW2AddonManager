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