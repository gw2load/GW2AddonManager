package com.gw2tb.manager.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gw2tb.manager.ui.composables.GameDirectoryPicker

@Composable
fun SettingsScreen(component: SettingsComponent) {
    Column(
        modifier = Modifier
            .widthIn(max = 400.dp)
    ) {
        val selectedGameDirectory by component.selectedGameDirectory.collectAsState()

        GameDirectoryPicker(
            selectedGameDirectory = selectedGameDirectory,
            selectGameDirectory = component::selectGameDirectory
        )
    }
}