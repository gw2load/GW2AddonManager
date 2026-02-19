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
package com.gw2tb.manager.services

import com.gw2tb.manager.exceptions.ManagerException
import com.gw2tb.manager.model.ManagerVersion
import kotlinx.coroutines.flow.Flow

/** The update service is responsible for updates to the manager itself. */
interface UpdateService {

    /**
     * The latest available version of the manager that is newer than the current one, or `null`.
     *
     * To avoid excessive network requests, the result may be cached for an unspecified amount of time. If fresh results
     * are required, call [refresh] explicitly.
     */
    val availableUpdate: Flow<ManagerVersion?>

    val managerManifestException: Flow<ManagerException?>

    /** Forces a refresh of the [availableUpdate]. */
    suspend fun refresh()

}
