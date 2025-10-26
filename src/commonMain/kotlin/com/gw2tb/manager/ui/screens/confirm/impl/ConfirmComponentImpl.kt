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
package com.gw2tb.manager.ui.screens.confirm.impl

import com.arkivanov.decompose.ComponentContext
import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.InstalledAddOn
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.catalog.isMatching
import com.gw2tb.manager.services.AddOnService
import com.gw2tb.manager.ui.screens.confirm.ConfirmComponent
import com.gw2tb.manager.ui.screens.confirm.ConfirmComponent.Output
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ConfirmComponentImpl(
    private val addOnService: AddOnService,
    override val plan: ActionPlan,
    mainContext: CoroutineContext,
    componentContext: ComponentContext,
    private val output: (Output) -> Unit
) : ConfirmComponent, ComponentContext by componentContext {

    private val coroutineScope = CoroutineScope(mainContext + SupervisorJob())

    init {
        /* If the list of locally installed add-ons changes, we discard the confirmation dialog. */
        var isFirst = true

        coroutineScope.launch {
            addOnService.localAddOns
                .collect {
                    if (isFirst) {
                        isFirst = false
                    } else {
                        withContext(Dispatchers.Main) {
                            cancel()
                        }
                    }
                }
        }
    }

    override fun getAddOnListing(id: AddOnId): StateFlow<AddOnListing?> =
        addOnService.addOnListings
            .map { listings -> listings.first { listing -> listing.id == id } }
            .stateIn(coroutineScope, SharingStarted.Eagerly, initialValue = null)

    override fun getInstalledAddOn(ref: LocalAddOnReference): StateFlow<InstalledAddOn?> {
        return addOnService.localAddOns
            .combine(addOnService.addOnListings) { a, b -> a to b }
            .map { (localAddOns, listings) ->
                val localAddOn = localAddOns.first { localAddOn -> localAddOn.ref == ref }

                InstalledAddOn(
                    localAddOn = localAddOn,
                    listing = listings.first { listing -> listing isMatching localAddOn }
                )
            }
            .stateIn(coroutineScope, SharingStarted.Eagerly, initialValue = null)
    }

    override fun cancel() {
        output(Output.Exit)
    }

    override fun confirm() {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Default) {
            addOnService.execute(plan)
        }

        output(Output.Exit)
    }

}
