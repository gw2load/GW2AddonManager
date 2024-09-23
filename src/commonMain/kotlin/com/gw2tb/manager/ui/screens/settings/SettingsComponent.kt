package com.gw2tb.manager.ui.screens.settings

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path

@Immutable
interface SettingsComponent {

    val selectedGameDirectory: StateFlow<Path?>

    fun selectGameDirectory(path: Path)

}