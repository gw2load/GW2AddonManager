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
package com.gw2tb.manager.services

import com.gw2tb.manager.model.LocalConfiguration
import com.gw2tb.manager.model.TempDirectoryLayout
import kotlinx.coroutines.flow.Flow

/**
 * The configuration services provides access to various configurations for the manager.
 *
 * The [localConfiguration] provides access to the local (i.e. PC-specific) configuration which contains information
 * such as the path to the game installation directory.
 */
interface ConfigurationService {

    /** The PC-specific local storage of the manager. */
    val localConfiguration: Flow<LocalConfiguration?>

    // TODO implement shared "settings" that contain release channel preferences

    /** The layout of the temporary directory. */
    val tempDirectoryLayout: TempDirectoryLayout

    fun isValid(localConfiguration: LocalConfiguration): Boolean {
        if (localConfiguration.selectedGameDirectory == null) return false
        return true
    }

    fun save(localConfiguration: LocalConfiguration)

}