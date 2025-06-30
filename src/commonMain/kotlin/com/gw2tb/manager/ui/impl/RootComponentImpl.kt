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
package com.gw2tb.manager.ui.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.gw2tb.manager.model.LocalConfiguration
import com.gw2tb.manager.model.Notification
import com.gw2tb.manager.services.*
import com.gw2tb.manager.ui.RootComponent
import com.gw2tb.manager.ui.RootComponent.Child
import com.gw2tb.manager.ui.screens.setup.SetupComponent
import com.gw2tb.manager.ui.screens.setup.impl.SetupComponentImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class RootComponentImpl(
    private val addOnService: AddOnService,
    configurationService: ConfigurationService,
    jobService: JobService,
    notificationService: NotificationService,
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    // TODO Properly scope coroutines
    private val mainContext = Dispatchers.Default
    private val coroutineScope = coroutineScope(mainContext + SupervisorJob())

    private val localConfiguration: StateFlow<LocalConfiguration?> =
        configurationService.localConfiguration.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = null)

    override val notifications: StateFlow<List<Notification>> =
        notificationService.notifications.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    private val navigation = StackNavigation<Config>()

    override val page: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = null,
        initialConfiguration = localConfiguration.value.let { localConfiguration ->
            /* If there is no local configuration yet, or if it's invalid, we'll start in the setup screen. */
            if (localConfiguration == null || !configurationService.isValid(localConfiguration)) Config.Setup else Config.Main
        },
        handleBackButton = false,
        childFactory = { config, componentContext ->
            when (config) {
                is Config.Main -> Child.Main(MainComponentImpl(
                    addOnService = addOnService,
                    configurationService = configurationService,
                    jobService = jobService,
                    mainContext = mainContext,
                    componentContext = componentContext
                ))
                is Config.Setup -> Child.Setup(SetupComponentImpl(
                    configurationService = configurationService,
                    output = { output ->
                        when (output) {
                            is SetupComponent.Output.ConfirmSetup -> navigation.replaceCurrent(Config.Main)
                        }
                    },
                    componentContext = componentContext
                ))
            }
        }
    )

    @Serializable
    private sealed class Config {

        @Serializable
        data object Main : Config()

        @Serializable
        data object Setup : Config()

    }

    override fun navigateToSettings() {
        navigation.replaceCurrent(Config.Setup)
    }

    override fun onNotificationClick(notification: Notification) {
        coroutineScope.launch {
            notification.quickFix?.invoke()
        }
    }

}