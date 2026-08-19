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

import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.actions.OperationResult
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.exceptions.ManagerException
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.InstalledAddOn
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.local.LocalAddOn
import kotlinx.coroutines.flow.Flow

interface AddOnService {

    val addOnListings: Flow<List<AddOnListing>>

    val addOnListingManifestException: Flow<ManagerException?>

    val allLocalAddOns: Flow<List<LocalAddOn>>

    val localAddOns: Flow<List<LocalAddOn>>

    val installedAddOns: Flow<List<InstalledAddOn>>

    suspend fun execute(plan: ActionPlan): OperationResult

    suspend fun disableAddOns(refs: Iterable<LocalAddOnReference>): OperationResult

    suspend fun enableAddOns(refs: Iterable<LocalAddOnReference>): OperationResult

    suspend fun installAddOns(ids: Iterable<AddOnId>): OperationResult

    suspend fun uninstallAddOns(refs: Iterable<LocalAddOnReference>): OperationResult

    suspend fun updateAddOns(updates: Iterable<AvailableAddOnUpdate>): OperationResult

    suspend fun refresh()

}
