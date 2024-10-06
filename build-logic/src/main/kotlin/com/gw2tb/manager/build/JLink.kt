/*
 * Copyright (c) 2019-2024 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.gw2tb.manager.build

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.process.ExecOperations
import java.io.*
import javax.inject.Inject

@CacheableTask
open class JLink @Inject constructor(
    private val execOperations: ExecOperations,
    private val fsOperations: FileSystemOperations,
    objects: ObjectFactory
) : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    val executable: RegularFileProperty = objects.fileProperty()

    @get:InputFiles
    @get:Classpath
    val modulePath: ConfigurableFileCollection = objects.fileCollection()

    @get:Input
    val addModules: ListProperty<String> = objects.listProperty()

    @get:Input
    val noHeaderFiles: Property<Boolean> = objects.property()

    @get:Input
    val noManPages: Property<Boolean> = objects.property()

    @get:OutputDirectory
    val destinationDir: DirectoryProperty = objects.directoryProperty()

    init {
        noHeaderFiles.convention(true)
        noManPages.convention(true)
    }

    @TaskAction
    protected fun execute() {
        executable.finalizeValue()
        modulePath.finalizeValue()
        addModules.finalizeValue()
        noHeaderFiles.finalizeValue()
        noManPages.finalizeValue()
        destinationDir.finalizeValue()

        val destinationDir = this@JLink.destinationDir.get().asFile

        fsOperations.delete {
            delete(destinationDir)
        }

        val addModules = this@JLink.addModules.getOrElse(emptyList())

        execOperations.exec {
            executable = this@JLink.executable.get().asFile.absolutePath

            if (!modulePath.isEmpty) args("-p", modulePath.files.joinToString(separator = File.pathSeparator) { it.absolutePath })
            args("--output", destinationDir)
            args("--compress", "zip-6")
            args("--disable-plugin", "release-info")
            args("--vm=server")

            if (addModules.isNotEmpty()) args("--add-modules", addModules.joinToString(separator = ","))
            if (noHeaderFiles.get()) args("--no-header-files")
            if (noManPages.get()) args("--no-man-pages")
        }
    }

}