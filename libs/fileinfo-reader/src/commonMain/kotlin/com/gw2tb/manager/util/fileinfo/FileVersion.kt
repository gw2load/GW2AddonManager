/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2025 Leon Linhart
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
package com.gw2tb.manager.util.fileinfo

data class FileVersion(
    val h1: UShort,
    val l1: UShort,
    val h2: UShort,
    val l2: UShort
) : Comparable<FileVersion> {

    override fun compareTo(other: FileVersion): Int = when {
        this === other -> 0
        this.h1 > other.h1 -> 1
        this.h1 < other.h1 -> -1
        this.l1 > other.l1 -> 1
        this.l1 < other.l1 -> -1
        this.h2 > other.h2 -> 1
        this.h2 < other.h2 -> -1
        this.l2 > other.l2 -> 1
        this.l2 < other.l2 -> -1
        else -> 0
    }

    override fun toString(): String = "$h1.$l1.$h2.$l2"

}
