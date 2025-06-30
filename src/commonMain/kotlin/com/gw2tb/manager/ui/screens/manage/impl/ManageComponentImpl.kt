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
package com.gw2tb.manager.ui.screens.manage.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.catalog.isMatching
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.services.AddOnService
import com.gw2tb.manager.services.ConfigurationService
import com.gw2tb.manager.services.Job
import com.gw2tb.manager.services.JobService
import com.gw2tb.manager.ui.screens.manage.InstalledAddOn
import com.gw2tb.manager.ui.screens.manage.ManageComponent
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import kotlin.coroutines.CoroutineContext

class ManageComponentImpl(
    private val addOnService: AddOnService,
    private val configurationService: ConfigurationService,
    jobService: JobService,
    selectedAddOnName: String?,
    mainContext: CoroutineContext,
    componentContext: ComponentContext
) : ManageComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope(mainContext + SupervisorJob())

    override val addOnListings: StateFlow<List<AddOnListing>> =
        addOnService.addOnListings.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    private val localAddOns: StateFlow<List<LocalAddOn>> =
        addOnService.localAddOns.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override val installedAddOns: StateFlow<List<InstalledAddOn>> =
        localAddOns
            .combine(addOnListings) { localAddOns, addOnListings ->
               localAddOns
                    .map { localAddOn ->
                        val listing = addOnListings.find { it isMatching localAddOn }
                        InstalledAddOn(localAddOn, listing)
                    }
                    .sortedBy { (localAddOn, addOnListing) -> addOnListing?.addOnName ?: localAddOn.name }
            }
            .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override val jobs: StateFlow<List<Job>> =
        jobService.jobs.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override val availableUpdates: StateFlow<List<AvailableAddOnUpdate>> =
        addOnService.availableUpdates.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    // TODO Figure out how to properly handle duplicate installs
    private val _selectedAddOn = MutableStateFlow(value = selectedAddOnName)
    override val selectedAddOn: StateFlow<LocalAddOn?> = localAddOns
        .combine(_selectedAddOn) { listings, selected -> listings.firstOrNull { it.name == selected } }
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

    override fun selectAddOn(localAddOn: LocalAddOn) {
        _selectedAddOn.value = localAddOn.name
    }

    override fun navigateToVendor(vendor: String) {
        Desktop.getDesktop().browse(URI(vendor))
    }

}