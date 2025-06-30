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
package com.gw2tb.manager.ui.screens.explore.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.services.AddOnService
import com.gw2tb.manager.services.ConfigurationService
import com.gw2tb.manager.services.Job
import com.gw2tb.manager.services.JobService
import com.gw2tb.manager.ui.screens.explore.ExploreComponent
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import kotlin.coroutines.CoroutineContext

class ExploreComponentImpl(
    private val addOnService: AddOnService,
    private val configurationService: ConfigurationService,
    jobService: JobService,
    selectedAddOnId: String? = null,
    mainContext: CoroutineContext,
    componentContext: ComponentContext
) : ExploreComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope(mainContext + SupervisorJob())

    override val addOnListings: StateFlow<List<AddOnListing>> =
        addOnService.addOnListings
            .map { it.filter { listing -> listing.download != null } }
            .map { it.sortedBy(AddOnListing::addOnName) }
            .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override val localAddOns: StateFlow<List<LocalAddOn>> =
        addOnService.localAddOns.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override val jobs: StateFlow<List<Job>> =
        jobService.jobs.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override val availableUpdates: StateFlow<List<AvailableAddOnUpdate>> =
        addOnService.availableUpdates.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    private val _selectedAddOn = MutableStateFlow(value = selectedAddOnId)
    override val selectedAddOn: StateFlow<AddOnListing?> = addOnListings
        .combine(_selectedAddOn) { listings, selected -> listings.firstOrNull { it.id == selected } }
        .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = null)

    override fun disable(addOn: LocalAddOn) {
        coroutineScope.launch {
            addOnService.disable(addOn)
        }
    }

    override fun enable(addOn: LocalAddOn) {
        coroutineScope.launch {
            addOnService.enable(addOn)
        }
    }

    override fun install(listing: AddOnListing) {
        coroutineScope.launch {
            val localConfiguration = configurationService.localConfiguration.first()
            val gameDirectory = localConfiguration?.selectedGameDirectory ?: error("Game directory should not be null")

            addOnService.install(
                listing = listing,
                gameDirectory = gameDirectory
            )
        }
    }

    override fun uninstall(addOn: LocalAddOn) {
        coroutineScope.launch {
            addOnService.uninstall(addOn)
        }
    }

    override fun selectAddOn(listing: AddOnListing) {
        _selectedAddOn.value = listing.id
    }

    override fun navigateToVendor(vendor: String) {
        Desktop.getDesktop().browse(URI(vendor))
    }

}