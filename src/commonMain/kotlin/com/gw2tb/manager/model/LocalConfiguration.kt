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

import com.gw2tb.manager.util.serialization.PathSerializer
import kotlinx.serialization.Serializable
import java.nio.file.Path

/**
 * The local storage of the manager contains PC-specific information (such as the currently selected game directory).
 *
 * @param autoUpdate            whether the manager should automatically update add-ons before starting the game
 * @param selectedGameDirectory the currently selected game directory
 * @param gameDirectories       a list of known or previously selected game directories
 */
@Serializable
data class LocalConfiguration(
    val autoUpdate: Boolean = true,
    val selectedGameDirectory: @Serializable(with = PathSerializer::class) Path? = null,
    val gameDirectories: List<@Serializable(with = PathSerializer::class) Path>
)
