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
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.gw2tb.manager.model.LocalConfiguration
import com.gw2tb.manager.services.AddOnService
import com.gw2tb.manager.services.ConfigurationService
import com.gw2tb.manager.services.Job
import com.gw2tb.manager.services.JobService
import com.gw2tb.manager.ui.MainComponent
import com.gw2tb.manager.ui.MainComponent.Child
import com.gw2tb.manager.ui.MasterDetailComponent
import com.gw2tb.manager.ui.RootComponent
import com.gw2tb.manager.ui.screens.settings.impl.SettingsComponentImpl
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import java.awt.Desktop
import java.net.URI
import kotlin.coroutines.CoroutineContext

class MainComponentImpl(
    private val addOnService: AddOnService,
    private val configurationService: ConfigurationService,
    private val jobService: JobService,
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

    private val navigation = StackNavigation<Config>()

    override val page: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = null,
        initialConfiguration = Config.MasterDetail(
            child = MasterDetailComponent.Config.ExploreAddOns()
        ),
        handleBackButton = false,
        childFactory = { config, componentContext ->
            when (config) {
                is Config.MasterDetail -> Child.MasterDetail(MasterDetailComponentImpl(
                    addOnService = addOnService,
                    configurationService = configurationService,
                    jobService = jobService,
                    mainContext = mainContext,
                    output = { output ->
                        when (output) {
                            is MasterDetailComponent.Output.NavigateToSettings -> navigateToSettings()
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

    override fun navigateToExploreAddOns() {
        val activeChild = page.active.instance
        if (activeChild is Child.MasterDetail) {
            activeChild.component.navigateToExploreAddOns()
            return
        }

        navigation.replaceCurrent(Config.MasterDetail(
            child = MasterDetailComponent.Config.ExploreAddOns()
        ))
    }

    override fun navigateToInstalledAddOns() {
        val activeChild = page.active.instance
        if (activeChild is Child.MasterDetail) {
            activeChild.component.navigateToInstalledAddOns()
            return
        }

        navigation.replaceCurrent(Config.MasterDetail(
            child = MasterDetailComponent.Config.ManageAddOns()
        ))
    }

    override fun navigateToSettings() {
        navigation.replaceCurrent(Config.Settings)
    }

    override fun openLink(url: String) {
        Desktop.getDesktop().browse(URI(url))
    }

    override fun play() {
        val localConfiguration = localConfiguration.value ?: error("Local configuration is not available")
        val gameDirectory = localConfiguration.selectedGameDirectory ?: error("No game directory selected")

        Desktop.getDesktop().open(gameDirectory.resolve("Gw2-64.exe").toFile())
    }

    override fun setAutoUpdatesEnabled(enabled: Boolean) = runBlocking {
        val localConfiguration = configurationService.localConfiguration.first() ?: error("No local configuration found")

        configurationService.save(localConfiguration.copy(
            autoUpdate = enabled
        ))
    }

}