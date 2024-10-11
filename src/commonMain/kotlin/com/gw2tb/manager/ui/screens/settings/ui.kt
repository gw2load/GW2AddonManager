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