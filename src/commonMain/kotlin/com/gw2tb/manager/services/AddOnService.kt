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
package com.gw2tb.manager.services

import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.local.LocalAddOn
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

interface AddOnService {

    val addOnListings: Flow<List<AddOnListing>>

    val localAddOns: Flow<List<LocalAddOn>>

    val availableUpdates: Flow<List<AvailableAddOnUpdate>>

    suspend fun disable(addOn: LocalAddOn)

    suspend fun enable(addOn: LocalAddOn)

    suspend fun install(listing: AddOnListing, gameDirectory: Path)

    suspend fun uninstall(addOn: LocalAddOn)

    suspend fun refreshListings()

}