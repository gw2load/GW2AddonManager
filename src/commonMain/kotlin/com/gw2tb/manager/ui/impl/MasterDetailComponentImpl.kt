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
package com.gw2tb.manager.ui.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.gw2tb.manager.services.*
import com.gw2tb.manager.ui.MasterDetailComponent
import com.gw2tb.manager.ui.MasterDetailComponent.*
import com.gw2tb.manager.ui.screens.explore.impl.ExploreComponentImpl
import com.gw2tb.manager.ui.screens.manage.impl.ManageComponentImpl
import kotlin.coroutines.CoroutineContext

class MasterDetailComponentImpl(
    private val addOnService: AddOnService,
    private val configurationService: ConfigurationService,
    private val jobService: JobService,
    private val mainContext: CoroutineContext,
    private val output: (Output) -> Unit,
    initialConfiguration: Config,
    componentContext: ComponentContext
) : MasterDetailComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val page: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = null,
        initialConfiguration = initialConfiguration,
        handleBackButton = false,
        childFactory = { config, _ ->
            when (config) {
                is Config.ExploreAddOns -> Child.ExploreAddOns(ExploreComponentImpl(
                    addOnService = addOnService,
                    configurationService = configurationService,
                    jobService = jobService,
                    selectedAddOnId = config.selectedAddOnId,
                    mainContext = mainContext,
                    componentContext = componentContext
                ))
                is Config.ManageAddOns -> Child.InstalledAddOns(ManageComponentImpl(
                    addOnService = addOnService,
                    configurationService = configurationService,
                    jobService = jobService,
                    selectedAddOnName = config.selectedAddOnName,
                    mainContext = mainContext,
                    componentContext = componentContext
                ))
            }
        }
    )

    override fun navigateToExploreAddOns() {
        val activeChild = page.active.instance
        val selectedAddOnId = let {
            if (activeChild is Child.InstalledAddOns) {
                val selectedAddOn = activeChild.component.selectedAddOn.value ?: return@let null
                val addOnListings = activeChild.component.addOnListings.value

                addOnListings.firstOrNull { selectedAddOn.name in it.addOnNames }?.id
            } else {
                null
            }
        }

        navigation.replaceCurrent(Config.ExploreAddOns(selectedAddOnId))
    }

    override fun navigateToInstalledAddOns() {
        val activeChild = page.active.instance
        val selectedAddOnId = let {
            if (activeChild is Child.ExploreAddOns) {
                val selectedAddOn = activeChild.component.selectedAddOn.value ?: return@let null
                val localAddOns = activeChild.component.localAddOns.value

                localAddOns.firstOrNull { it.name in selectedAddOn.addOnNames }?.name
            } else {
                null
            }
        }
        navigation.replaceCurrent(Config.ManageAddOns(selectedAddOnId))
    }

    override fun navigateToSettings() {
        output(Output.NavigateToSettings)
    }

}