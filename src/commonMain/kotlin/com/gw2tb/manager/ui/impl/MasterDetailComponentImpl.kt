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
package com.gw2tb.manager.ui.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.exceptions.ManagerException
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.services.*
import com.gw2tb.manager.ui.MasterDetailComponent
import com.gw2tb.manager.ui.MasterDetailComponent.*
import com.gw2tb.manager.ui.screens.confirm.ConfirmComponent
import com.gw2tb.manager.ui.screens.confirm.impl.ConfirmComponentImpl
import com.gw2tb.manager.ui.screens.details.AddOnDetailsComponent
import com.gw2tb.manager.ui.screens.details.impl.AddOnDetailsComponentImpl
import com.gw2tb.manager.ui.screens.exception.impl.ExceptionComponentImpl
import com.gw2tb.manager.ui.screens.explore.ExploreComponent
import com.gw2tb.manager.ui.screens.explore.impl.ExploreComponentImpl
import com.gw2tb.manager.ui.screens.manage.ManageComponent
import com.gw2tb.manager.ui.screens.manage.impl.ManageComponentImpl
import kotlin.coroutines.CoroutineContext

class MasterDetailComponentImpl(
    private val addOnService: AddOnService,
    private val configurationService: ConfigurationService,
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
                    configurationService = configurationService,
                    inspectionService = inspectionService,
                    addOnId = config.addOnId,
                    localAddOnRef = config.ref,
                    mainContext = mainContext,
                    componentContext = componentContext,
                    output = { output ->
                        when (output) {
                            is AddOnDetailsComponent.Output.Exit -> navigation.pop()
                            is AddOnDetailsComponent.Output.NavigateToVendor -> output(Output.OpenUrl(url = output.url))
                            is AddOnDetailsComponent.Output.RequiresConfirmation -> navigateToConfirm(plan = output.plan)
                        }
                    }
                ))
                is Config.Confirm -> Child.Confirm(ConfirmComponentImpl(
                    addOnService = addOnService,
                    configurationService = configurationService,
                    plan = config.plan,
                    mainContext = mainContext,
                    componentContext = componentContext,
                    output = { output ->
                        when (output) {
                            is ConfirmComponent.Output.Exit -> navigation.pop()
                        }
                    }
                ))
                is Config.Exception -> Child.Exception(ExceptionComponentImpl(
                    exception = config.exception,
                    mainContext = mainContext,
                    componentContext = componentContext
                ))
                is Config.ExploreAddOns -> Child.ExploreAddOns(ExploreComponentImpl(
                    addOnService = addOnService,
                    inspectionService = inspectionService,
                    jobService = jobService,
                    mainContext = mainContext,
                    componentContext = componentContext,
                    output = { output ->
                        when (output) {
                            is ExploreComponent.Output.NavigateToDetailsById -> navigateToAddOnDetails(output.addOnId)
                            is ExploreComponent.Output.NavigateToDetailsByRef -> navigateToAddOnDetails(output.ref)
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
                            is ManageComponent.Output.NavigateToDetails -> navigateToAddOnDetails(output.localAddOn)
                            is ManageComponent.Output.RequiresConfirmation -> navigateToConfirm(plan = output.plan)
                        }
                    }
                ))
            }
        }
    )

    override fun navigateToAddOnDetails(id: AddOnId) {
        navigation.bringToFront(Config.AddOnDetails(addOnId = id))
    }

    override fun navigateToAddOnDetails(ref: LocalAddOnReference) {
        navigation.bringToFront(Config.AddOnDetails(ref = ref))
    }

    override fun navigateToConfirm(plan: ActionPlan) {
        navigation.bringToFront(Config.Confirm(plan))
    }

    override fun navigateToException(exception: ManagerException) {
        navigation.replaceAll(Config.Exception(exception))
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
