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
package com.gw2tb.manager.ui.screens.explore

import androidx.compose.runtime.Immutable
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.services.Job
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface ExploreComponent {

    val addOnListings: StateFlow<List<AddOnListing>>

    val localAddOns: StateFlow<List<LocalAddOn>>

    val availableUpdates: StateFlow<List<AvailableAddOnUpdate>>

    val jobs: StateFlow<List<Job>>

    val selectedAddOn: StateFlow<AddOnListing?>

    fun disable(addOn: LocalAddOn)

    fun enable(addOn: LocalAddOn)

    fun setEnabled(addOn: LocalAddOn, enabled: Boolean) =
        if (enabled) enable(addOn) else disable(addOn)

    fun install(listing: AddOnListing)

    fun uninstall(addOn: LocalAddOn)

    fun selectAddOn(listing: AddOnListing)

    fun navigateToVendor(vendor: String)

}