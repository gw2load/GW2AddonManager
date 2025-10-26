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
import com.gw2tb.manager.actions.OperationResult
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.inspections.InspectionAddOnUpdateAvailable
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.services.AddOnService
import com.gw2tb.manager.services.InspectionService
import com.gw2tb.manager.services.Job
import com.gw2tb.manager.services.JobService
import com.gw2tb.manager.ui.screens.explore.ExploreComponent
import com.gw2tb.manager.ui.screens.explore.ExploreComponent.Output
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.map
import kotlin.coroutines.CoroutineContext

class ExploreComponentImpl(
    private val addOnService: AddOnService,
    inspectionService: InspectionService,
    jobService: JobService,
    mainContext: CoroutineContext,
    componentContext: ComponentContext,
    private val output: (Output) -> Unit
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
        inspectionService.inspectionsByType(InspectionAddOnUpdateAvailable)
            .map { inspection -> inspection.map(InspectionAddOnUpdateAvailable::update) }
            .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override fun disableAddOn(ref: LocalAddOnReference) {
        coroutineScope.launch {
            val result = addOnService.disableAddOns(listOf(ref))
            if (result is OperationResult.RequiresConfirmation) {
                withContext(Dispatchers.Main) {
                    output(Output.RequiresConfirmation(result.plan))
                }
            }
        }
    }

    override fun enableAddOn(ref: LocalAddOnReference) {
        coroutineScope.launch {
            val result = addOnService.enableAddOns(listOf(ref))
            if (result is OperationResult.RequiresConfirmation) {
                withContext(Dispatchers.Main) {
                    output(Output.RequiresConfirmation(result.plan))
                }
            }
        }
    }

    override fun installAddOn(id: AddOnId) {
        coroutineScope.launch {
            val result = addOnService.installAddOns(listOf(id))
            if (result is OperationResult.RequiresConfirmation) {
                withContext(Dispatchers.Main) {
                    output(Output.RequiresConfirmation(result.plan))
                }
            }
        }
    }

    override fun uninstallAddOn(ref: LocalAddOnReference) {
        coroutineScope.launch {
            val result = addOnService.uninstallAddOns(listOf(ref))
            if (result is OperationResult.RequiresConfirmation) {
                withContext(Dispatchers.Main) {
                    output(Output.RequiresConfirmation(result.plan))
                }
            }
        }
    }

    override fun updateAddOn(update: AvailableAddOnUpdate) {
        coroutineScope.launch {
            val result = addOnService.updateAddOns(listOf(update))
            if (result is OperationResult.RequiresConfirmation) {
                withContext(Dispatchers.Main) {
                    output(Output.RequiresConfirmation(result.plan))
                }
            }
        }
    }

    override fun navigateToAddOnDetails(id: AddOnId) {
        output(Output.NavigateToDetails(id))
    }

}
