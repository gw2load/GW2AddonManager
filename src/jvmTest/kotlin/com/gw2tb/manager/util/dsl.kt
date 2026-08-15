/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2026 Leon Linhart
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
package com.gw2tb.manager.util

import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.name

fun setupGameDirectory(gameDirectory: Path, configure: DirectoryConfigurer.() -> Unit) {
    val configurer = DirectoryConfigurer(Directory(gameDirectory.name))
    configurer.configure()

    val queue = ArrayDeque(configurer.directory.entries.map { GenEntry(gameDirectory, it) })
    while (queue.isNotEmpty()) {
        val (parent, entry) = queue.removeFirst()
        queue.addAll(entry.create(parent))
    }
}

data class GenEntry(val parent: Path, val entry: DirectoryEntry)

interface DirectoryEntry {

    fun create(parent: Path): List<GenEntry>

}

class Directory(
    val name: String
) : DirectoryEntry {

    val entries: MutableList<DirectoryEntry> = mutableListOf()

    override fun create(parent: Path): List<GenEntry> {
        val parent = parent.resolve(name).createDirectory()
        return entries.map { GenEntry(parent, it) }
    }

}

class File(
    val name: String
) : DirectoryEntry {

    override fun create(parent: Path): List<GenEntry> {
        parent.resolve(name).createFile()
        return emptyList()
    }

}

@DslMarker
annotation class GameDirectoryDsl

@GameDirectoryDsl
class DirectoryConfigurer internal constructor(
    val directory: Directory
) {

    fun dir(name: String, configure: DirectoryConfigurer.() -> Unit) {
        val directory = Directory(name)
            .also { directory.entries += it }

        DirectoryConfigurer(directory).configure()
    }

    fun addOn(name: String) {
        File(name)
            .also { directory.entries += it }
    }

}
