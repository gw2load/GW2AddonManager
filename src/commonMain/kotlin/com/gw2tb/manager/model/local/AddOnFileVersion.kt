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
package com.gw2tb.manager.model.local

import androidx.compose.runtime.Immutable

@Immutable
data class AddOnFileVersion(
    private val major: UShort,
    private val minor: UShort,
    private val patch: UShort,
    private val fix: UShort
) : Comparable<AddOnFileVersion> {

    override fun compareTo(other: AddOnFileVersion): Int = when {
        this === other -> 0
        this.major > other.major -> 1
        this.major < other.major -> -1
        this.minor > other.minor -> 1
        this.minor < other.minor -> -1
        this.patch > other.patch -> 1
        this.patch < other.patch -> -1
        this.fix > other.fix -> 1
        this.fix < other.fix -> -1
        else -> 0
    }

    override fun toString(): String = "$major.$minor.$patch.$fix"

}