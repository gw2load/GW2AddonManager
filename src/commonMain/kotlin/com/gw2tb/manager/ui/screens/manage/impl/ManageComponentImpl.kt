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
import com.gw2tb.manager.actions.OperationResult
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.catalog.isMatching
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.services.AddOnService
import com.gw2tb.manager.services.Job
import com.gw2tb.manager.services.JobService
import com.gw2tb.manager.model.InstalledAddOn
import com.gw2tb.manager.model.inspections.InspectionAddOnUpdateAvailable
import com.gw2tb.manager.services.InspectionService
import com.gw2tb.manager.ui.screens.manage.ManageComponent
import com.gw2tb.manager.ui.screens.manage.ManageComponent.Output
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ManageComponentImpl(
    private val addOnService: AddOnService,
    inspectionService: InspectionService,
    jobService: JobService,
    mainContext: CoroutineContext,
    componentContext: ComponentContext,
    private val output: (Output) -> Unit
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

    override fun navigateToDetails(ref: LocalAddOnReference) {
        output(Output.NavigateToDetails(ref))
    }

}
