/*
 * GW2AddOnManager
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
package com.gw2tb.manager.model.local

import androidx.compose.runtime.Immutable

@Immutable
data class AddOnVersion(
    val fileVersion: AddOnFileVersion,
    private val versionString: String = fileVersion.toString()
) : Comparable<AddOnVersion> {

    override fun compareTo(other: AddOnVersion): Int = fileVersion.compareTo(other.fileVersion)
    override fun toString(): String = versionString

}