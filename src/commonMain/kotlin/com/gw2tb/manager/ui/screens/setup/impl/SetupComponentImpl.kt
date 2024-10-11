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
package com.gw2tb.manager.ui.screens.setup.impl

import com.arkivanov.decompose.ComponentContext
import com.gw2tb.manager.discoverer.gamedir.GuessingGw2Discoverer
import com.gw2tb.manager.discoverer.gamedir.SteamGw2Discoverer
import com.gw2tb.manager.model.LocalConfiguration
import com.gw2tb.manager.services.ConfigurationService
import com.gw2tb.manager.ui.screens.setup.SetupComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

class SetupComponentImpl(
    private val configurationService: ConfigurationService,
    private val output: (SetupComponent.Output) -> Unit,
    componentContext: ComponentContext
) : SetupComponent, ComponentContext by componentContext {

    private companion object {

        private val log: Logger = LoggerFactory.getLogger(SetupComponentImpl::class.java)

        private val gw2Discoverers = listOf(
            GuessingGw2Discoverer(),
            SteamGw2Discoverer()
        )

    }

    private val coroutineContext = CoroutineScope(Dispatchers.Default)

    private val _selectedGameDirectory = MutableSharedFlow<Path>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val selectedGameDirectory: StateFlow<Path?> = _selectedGameDirectory
        .stateIn(coroutineContext, started = SharingStarted.Eagerly, initialValue = null)

    init {
        val suggestedGameDirectory = gw2Discoverers.firstNotNullOfOrNull { it.findGameDirectory() }

        if (suggestedGameDirectory != null) {
            log.debug("Suggesting game directory: {}", suggestedGameDirectory)
            _selectedGameDirectory.tryEmit(suggestedGameDirectory)
        } else {
            log.debug("No game directory found")
        }
    }

    override fun confirmSetup() {
        val selectedGameDirectory = selectedGameDirectory.value ?: run {
            log.error("No game directory selected")
            return
        }

        val localConfiguration = LocalConfiguration(
            selectedGameDirectory = selectedGameDirectory,
            gameDirectories = listOf(selectedGameDirectory)
        )

        configurationService.save(localConfiguration)
        output(SetupComponent.Output.ConfirmSetup)
    }

    override fun selectGameDirectory(path: Path) {
        _selectedGameDirectory.tryEmit(path)
    }

}