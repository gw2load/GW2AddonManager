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