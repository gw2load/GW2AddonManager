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
package com.gw2tb.manager.ui.screens.manage

import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.InstalledAddOn
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.inspections.Inspection
import com.gw2tb.manager.services.Job
import kotlinx.coroutines.flow.StateFlow

interface ManageComponent {

    val installedAddOns: StateFlow<List<InstalledAddOn>>

    val addOnListings: StateFlow<List<AddOnListing>>

    val inspections: StateFlow<Iterable<Inspection>>

    val jobs: StateFlow<List<Job>>

    fun disableAddOn(ref: LocalAddOnReference)

    fun enableAddOn(ref: LocalAddOnReference)

    fun setEnabled(ref: LocalAddOnReference, enabled: Boolean) =
        if (enabled) enableAddOn(ref) else disableAddOn(ref)

    fun repairAddOn(inspections: Iterable<Inspection>)

    fun uninstallAddOn(ref: LocalAddOnReference)

    fun updateAddOn(update: AvailableAddOnUpdate)

    fun navigateToDetails(ref: LocalAddOnReference)

    sealed interface Output {
        data class RequiresConfirmation(val plan: ActionPlan) : Output
        data class NavigateToDetails(val localAddOn: LocalAddOnReference) : Output
    }

}
