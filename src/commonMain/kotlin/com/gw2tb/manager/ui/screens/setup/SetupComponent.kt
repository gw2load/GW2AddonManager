package com.gw2tb.manager.ui.screens.setup

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path

@Immutable
interface SetupComponent {

    val selectedGameDirectory: StateFlow<Path?>

    fun confirmSetup()

    fun selectGameDirectory(path: Path)

    sealed class Output {
        data object ConfirmSetup : Output()
    }

}