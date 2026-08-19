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
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.exceptions.ManagerException
import com.gw2tb.manager.internal.BuildConfig
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.LocalConfiguration
import com.gw2tb.manager.inspections.InspectionAddOnUpdateAvailable
import com.gw2tb.manager.model.notifications.Urgency
import com.gw2tb.manager.services.AddOnService
import com.gw2tb.manager.services.ConfigurationService
import com.gw2tb.manager.services.InspectionService
import com.gw2tb.manager.services.Job
import com.gw2tb.manager.services.JobService
import com.gw2tb.manager.services.NotificationService
import com.gw2tb.manager.ui.MainComponent
import com.gw2tb.manager.ui.MainComponent.Child
import com.gw2tb.manager.ui.MasterDetailComponent
import com.gw2tb.manager.ui.screens.settings.impl.SettingsComponentImpl
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.awt.Desktop
import java.net.URI
import kotlin.coroutines.CoroutineContext

class MainComponentImpl(
    private val addOnService: AddOnService,
    private val configurationService: ConfigurationService,
    private val inspectionService: InspectionService,
    private val jobService: JobService,
    notificationService: NotificationService,
    private val mainContext: CoroutineContext,
    componentContext: ComponentContext
) : MainComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope(mainContext + SupervisorJob())

    private val localConfiguration: StateFlow<LocalConfiguration?> =
        configurationService.localConfiguration.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = null)

    override val areAutoUpdatesEnabled: StateFlow<Boolean> =
        configurationService.localConfiguration
            .map { it?.autoUpdate ?: false }
            .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = false)

    override val jobs: StateFlow<List<Job>> =
        jobService.jobs.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override val canPlay: StateFlow<Boolean> =
        notificationService.notifications
            .combine(jobs) { a, b -> a to b }
            .map { (notifications, jobs) -> notifications.none { it.urgency == Urgency.Critical } && jobs.isEmpty() }
            .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = false)

    private val navigation = StackNavigation<Config>()

    override val page: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = null,
        initialConfiguration = Config.MasterDetail(
            child = MasterDetailComponent.Config.ExploreAddOns
        ),
        handleBackButton = false,
        childFactory = { config, componentContext ->
            when (config) {
                is Config.MasterDetail -> Child.MasterDetail(MasterDetailComponentImpl(
                    addOnService = addOnService,
                    configurationService = configurationService,
                    inspectionService = inspectionService,
                    jobService = jobService,
                    mainContext = mainContext,
                    output = { output ->
                        when (output) {
                            is MasterDetailComponent.Output.NavigateToSettings -> navigateToSettings()
                            is MasterDetailComponent.Output.OpenUrl -> openLink(url = output.url)
                        }
                    },
                    initialConfiguration = config.child,
                    componentContext = componentContext
                ))
                is Config.Settings -> Child.Settings(SettingsComponentImpl(
                    configurationService = configurationService,
                    mainContext = mainContext,
                    componentContext = componentContext
                ))
            }
        }
    )

    @Serializable
    private sealed class Config {

        @Serializable
        data class MasterDetail(val child: MasterDetailComponent.Config) : Config()

        @Serializable
        data object Settings : Config()

    }

    override fun navigateToAddOnDetails(id: AddOnId) {
        val activeChild = page.active.instance
        if (activeChild is Child.MasterDetail) {
            activeChild.component.navigateToAddOnDetails(id)
            return
        }

        navigation.bringToFront(Config.MasterDetail(
            child = MasterDetailComponent.Config.AddOnDetails(addOnId = id)
        ))
    }

    override fun navigateToAddOnDetails(ref: LocalAddOnReference) {
        val activeChild = page.active.instance
        if (activeChild is Child.MasterDetail) {
            activeChild.component.navigateToAddOnDetails(ref)
            return
        }

        navigation.bringToFront(Config.MasterDetail(
            child = MasterDetailComponent.Config.AddOnDetails(ref = ref)
        ))
    }

    override fun navigateToConfirm(plan: ActionPlan) {
        val activeChild = page.active.instance
        if (activeChild is Child.MasterDetail) {
            activeChild.component.navigateToConfirm(plan)
            return
        }

        navigation.bringToFront(Config.MasterDetail(
            child = MasterDetailComponent.Config.Confirm(plan)
        ))
    }

    override fun navigateToException(exception: ManagerException) {
        val activeChild = page.active.instance
        if (activeChild is Child.MasterDetail) {
            activeChild.component.navigateToException(exception)
            return
        }

        navigation.replaceAll(Config.MasterDetail(
            child = MasterDetailComponent.Config.Exception(exception)
        ))
    }

    override fun navigateToExploreAddOns() {
        val activeChild = page.active.instance
        if (activeChild is Child.MasterDetail) {
            activeChild.component.navigateToExploreAddOns()
            return
        }

        navigation.replaceAll(Config.MasterDetail(
            child = MasterDetailComponent.Config.ExploreAddOns
        ))
    }

    override fun navigateToHelp() {
        openLink(BuildConfig.HELP_URL)
    }

    override fun navigateToInstalledAddOns() {
        val activeChild = page.active.instance
        if (activeChild is Child.MasterDetail) {
            activeChild.component.navigateToInstalledAddOns()
            return
        }

        navigation.replaceAll(Config.MasterDetail(
            child = MasterDetailComponent.Config.ManageAddOns
        ))
    }

    override fun navigateToSettings() {
        navigation.replaceAll(Config.Settings)
    }

    override fun openLink(url: String) {
        Desktop.getDesktop().browse(URI(url))
    }

    override fun play() {
        val localConfiguration = localConfiguration.value ?: error("Local configuration is not available")
        val gameDirectory = localConfiguration.selectedGameDirectory ?: error("No game directory selected")

        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Default) {
            val inspections = inspectionService.inspectionsByType(InspectionAddOnUpdateAvailable)
                .first()

            if (localConfiguration.autoUpdate) {
                addOnService.updateAddOns(inspections.map { it.update })
            }

            Desktop.getDesktop().open(gameDirectory.resolve("Gw2-64.exe").toFile())
        }
    }

    override fun setAutoUpdatesEnabled(enabled: Boolean) = runBlocking {
        val localConfiguration = configurationService.localConfiguration.first() ?: error("No local configuration found")

        configurationService.save(localConfiguration.copy(
            autoUpdate = enabled
        ))
    }

}
