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
package com.gw2tb.manager.model.local

import com.gw2tb.manager.util.fileinfo.FileVersion

data class AddOnVersion(
    val fileVersion: FileVersion,
    private val versionString: String = fileVersion.toString()
) : Comparable<AddOnVersion> {

    override fun compareTo(other: AddOnVersion): Int = fileVersion.compareTo(other.fileVersion)
    override fun toString(): String = versionString

}
