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