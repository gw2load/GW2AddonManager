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
package com.gw2tb.manager.ui.screens.details.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.gw2tb.manager.actions.OperationResult
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.catalog.isMatching
import com.gw2tb.manager.model.inspections.Inspection
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.services.AddOnService
import com.gw2tb.manager.services.ConfigurationService
import com.gw2tb.manager.services.InspectionService
import com.gw2tb.manager.ui.screens.details.AddOnDetailsComponent
import com.gw2tb.manager.ui.screens.details.AddOnDetailsComponent.Output
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.deleteExisting

class AddOnDetailsComponentImpl(
    private val addOnService: AddOnService,
    configurationService: ConfigurationService,
    inspectionService: InspectionService,
    private val addOnId: AddOnId?,
    private val localAddOnRef: LocalAddOnReference?,
    mainContext: CoroutineContext,
    componentContext: ComponentContext,
    private val output: (Output) -> Unit
) : AddOnDetailsComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope(mainContext + SupervisorJob())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val listing: StateFlow<AddOnListing?> = addOnService.addOnListings
        .mapLatest { listings -> listings.firstOrNull { (addOnId != null && it.id == addOnId) || (localAddOnRef != null && it isMatching localAddOnRef) } }
        .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = null)

    override val localAddOns: StateFlow<List<LocalAddOn>> =
        combine(addOnService.localAddOns, listing) { localAddOns, listing ->
            localAddOns.filter { (listing != null && listing isMatching it) || (localAddOnRef != null && it.ref == localAddOnRef) }
        }
        .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override val inspections: StateFlow<Iterable<Inspection>> =
        combine(localAddOns, inspectionService.inspections) { localAddOns, inspections ->
            inspections.values.flatten().filter { inspection -> localAddOns.any { it.ref in inspection.affectedRefs } }
        }
            .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override val selectedGameDirectory: StateFlow<Path?> = configurationService.localConfiguration
        .map { it!!.selectedGameDirectory }
        .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = null)

    override fun deleteAddOns(localAddOns: Iterable<LocalAddOn>) {
        for (localAddOn in localAddOns) {
            localAddOn.path.deleteExisting()
        }
    }

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

    override fun navigateToVendor(url: String) {
        output(Output.NavigateToVendor(url))
    }

}
