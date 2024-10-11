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
package com.gw2tb.manager.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.setup_confirm
import com.gw2tb.manager.gw2addonmanager.generated.resources.setup_welcome
import com.gw2tb.manager.ui.composables.EmphasisButton
import com.gw2tb.manager.ui.composables.GameDirectoryPicker
import org.jetbrains.compose.resources.stringResource

@Composable
fun SetupScreen(component: SetupComponent) {
    Column(
        modifier = Modifier
            .padding(end = 176.dp)
            .widthIn(max = 400.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(Res.string.setup_welcome),
            color = Color.Black.copy(alpha = ContentAlpha.medium)
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        val selectedGameDirectory by component.selectedGameDirectory.collectAsState()

        GameDirectoryPicker(
            selectedGameDirectory = selectedGameDirectory,
            selectGameDirectory = component::selectGameDirectory
        )

        Spacer(
            modifier = Modifier
                .height(32.dp)
        )

        EmphasisButton(
            onClick = component::confirmSetup,
            modifier = Modifier
                .width(182.dp)
                .align(Alignment.CenterHorizontally),
            enabled = selectedGameDirectory != null
        ) {
            Text(
                text = stringResource(Res.string.setup_confirm),
                fontSize = 24.sp
            )
        }
    }
}