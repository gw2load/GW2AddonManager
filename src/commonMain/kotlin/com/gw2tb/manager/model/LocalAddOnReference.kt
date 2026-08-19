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
package com.gw2tb.manager.model

import kotlinx.serialization.Serializable
import java.nio.file.Path

/**
 * A reference to a locally installed add-on.
 *
 * @param path the path identifying the add-on
 * @param name the name of the add-on
 */
@Serializable
class LocalAddOnReference(
    private val path: Path,
    val name: String
) {

    private val Path.normalized: Path
        get() = resolveSibling(fileName.toString().removeSuffix(".disabled"))

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is LocalAddOnReference -> false
        else -> path.normalized == other.path.normalized && name == other.name
    }

    override fun hashCode(): Int =
        31 * path.normalized.hashCode() + name.hashCode()

    override fun toString(): String =
        "LocalAddOnReference(path=$path, name='$name')"

}
