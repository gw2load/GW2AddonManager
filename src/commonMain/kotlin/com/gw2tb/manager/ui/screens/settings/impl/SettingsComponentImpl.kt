/*
 * GW2AddOnManager
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
package com.gw2tb.manager.ui.screens.settings.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.gw2tb.manager.services.ConfigurationService
import com.gw2tb.manager.ui.screens.settings.SettingsComponent
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

class SettingsComponentImpl(
    private val configurationService: ConfigurationService,
    mainContext: CoroutineContext,
    componentContext: ComponentContext
) : SettingsComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope(mainContext + SupervisorJob())

    override val selectedGameDirectory: StateFlow<Path?> = configurationService.localConfiguration
        .map { it?.selectedGameDirectory }
        .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = null)

    override fun selectGameDirectory(path: Path): Unit = runBlocking {
        val localConfiguration = configurationService.localConfiguration.first() ?: error("No local configuration found")

        configurationService.save(localConfiguration.copy(
            selectedGameDirectory = path
        ))
    }

}