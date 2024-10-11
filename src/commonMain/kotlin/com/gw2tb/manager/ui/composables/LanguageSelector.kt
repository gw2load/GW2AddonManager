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
package com.gw2tb.manager.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import java.awt.Cursor

@Composable
fun LanguageSelector() {
    var isDropdownVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { isDropdownVisible = !isDropdownVisible }
            )
            .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            .border(1.dp, color = lerp(Color(0xFF8ad3d3), Color.Black, 0.4F))
    ) {
        Row(
            modifier = Modifier
                .background(brush = Brush.verticalGradient(listOf(Color.White, Color(0xFF8ad3d3))))
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            Text(
                text = "LANGUAGE",
                fontSize = 10.sp
            )
        }

        Row(
            modifier = Modifier
                .background(brush = Brush.verticalGradient(listOf(lerp(Color(0xFF8ad3d3), Color.Black, 0.4F), Color(0xFF8ad3d3))))
                .padding(4.dp),
        ) {
            Text(
                text = "EN",
                color = Color.White,
                fontSize = 10.sp
            )
        }

        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(0.dp))
        ) {
            DropdownMenu(
                expanded = isDropdownVisible,
                onDismissRequest = { isDropdownVisible = false },
                modifier = Modifier
                    .background(Color.White, shape = RectangleShape),
                properties = @OptIn(ExperimentalComposeUiApi::class) PopupProperties(
                    usePlatformInsets = false
                )
            ) {
                Column(
                    modifier = Modifier
                        .background(color = Color.White, shape = RectangleShape)
                ) {
                    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 10.sp)) {
                        Text("ENGLISH", modifier = Modifier.background(Color.Red))
                        Text("GERMAN")
                    }
                }
            }
        }
    }
}