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
package com.gw2tb.manager.ui.screens.details

import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.inspections.Inspection
import com.gw2tb.manager.model.local.LocalAddOn
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path

interface AddOnDetailsComponent {

    val listing: StateFlow<AddOnListing?>

    val localAddOns: StateFlow<List<LocalAddOn>>

    val inspections: StateFlow<Iterable<Inspection>>

    val selectedGameDirectory: StateFlow<Path?>

    fun deleteAddOns(localAddOns: Iterable<LocalAddOn>)

    fun disableAddOn(ref: LocalAddOnReference)

    fun enableAddOn(ref: LocalAddOnReference)

    fun installAddOn(id: AddOnId)

    fun uninstallAddOn(ref: LocalAddOnReference)

    fun updateAddOn(update: AvailableAddOnUpdate)

    fun navigateToVendor(url: String)

    sealed interface Output {
        data class NavigateToVendor(val url: String) : Output
        data class RequiresConfirmation(val plan: ActionPlan) : Output
    }

}
