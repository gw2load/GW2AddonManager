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
import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.services.*
import com.gw2tb.manager.ui.MasterDetailComponent
import com.gw2tb.manager.ui.MasterDetailComponent.*
import com.gw2tb.manager.ui.screens.confirm.ConfirmComponent
import com.gw2tb.manager.ui.screens.confirm.impl.ConfirmComponentImpl
import com.gw2tb.manager.ui.screens.details.impl.AddOnDetailsComponentImpl
import com.gw2tb.manager.ui.screens.explore.ExploreComponent
import com.gw2tb.manager.ui.screens.explore.impl.ExploreComponentImpl
import com.gw2tb.manager.ui.screens.manage.ManageComponent
import com.gw2tb.manager.ui.screens.manage.impl.ManageComponentImpl
import kotlin.coroutines.CoroutineContext

class MasterDetailComponentImpl(
    private val addOnService: AddOnService,
    private val inspectionService: InspectionService,
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
                is Config.AddOnDetails -> Child.AddOnDetails(AddOnDetailsComponentImpl(
                    addOnService = addOnService,
                    addOnId = config.addOnId,
                    localAddOnRef = config.ref,
                    mainContext = mainContext,
                    componentContext = componentContext
                ))
                is Config.Confirm -> Child.Confirm(ConfirmComponentImpl(
                    addOnService = addOnService,
                    plan = config.plan,
                    mainContext = mainContext,
                    componentContext = componentContext,
                    output = { output ->
                        when (output) {
                            is ConfirmComponent.Output.Exit -> navigation.pop()
                        }
                    }
                ))
                is Config.ExploreAddOns -> Child.ExploreAddOns(ExploreComponentImpl(
                    addOnService = addOnService,
                    inspectionService = inspectionService,
                    jobService = jobService,
                    mainContext = mainContext,
                    componentContext = componentContext,
                    output = { output ->
                        when (output) {
                            is ExploreComponent.Output.NavigateToDetails -> navigation.pushToFront(Config.AddOnDetails(addOnId = output.addOnId))
                            is ExploreComponent.Output.RequiresConfirmation -> navigateToConfirm(plan = output.plan)
                        }
                    }
                ))
                is Config.ManageAddOns -> Child.InstalledAddOns(ManageComponentImpl(
                    addOnService = addOnService,
                    inspectionService = inspectionService,
                    jobService = jobService,
                    mainContext = mainContext,
                    componentContext = componentContext,
                    output = { output ->
                        when (output) {
                            is ManageComponent.Output.NavigateToDetails -> navigation.pushToFront(Config.AddOnDetails(ref = output.localAddOn))
                            is ManageComponent.Output.RequiresConfirmation -> navigateToConfirm(plan = output.plan)
                        }
                    }
                ))
            }
        }
    )

    override fun navigateToConfirm(plan: ActionPlan) {
        navigation.pushToFront(Config.Confirm(plan))
    }

    override fun navigateToExploreAddOns() {
        navigation.replaceAll(Config.ExploreAddOns)
    }

    override fun navigateToInstalledAddOns() {
        navigation.replaceAll(Config.ManageAddOns)
    }

    override fun navigateToSettings() {
        output(Output.NavigateToSettings)
    }

}
