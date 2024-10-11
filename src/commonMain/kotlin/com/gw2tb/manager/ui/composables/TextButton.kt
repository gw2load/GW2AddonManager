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
package com.gw2tb.manager.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import org.jetbrains.skiko.Cursor

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8ad3d3),
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null
) =
    TextButton(
        text = {
            Text(
                text = text,
                softWrap = false,
                maxLines = 1
            )
        },
        onClick = onClick,
        modifier = modifier,
        color = color,
        leadingIcon = leadingIcon,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role
    )

@Composable
fun TextButton(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8ad3d3),
    idleColor: Color = lerp(color, Color.Black, 0.4F),
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    @Suppress("NAME_SHADOWING")
    val color = if (isHovered) color else idleColor

    CompositionLocalProvider(LocalContentColor provides color) {
        Row(
            modifier = modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClickLabel = onClickLabel,
                    role = role,
                    onClick = onClick
                )
                .then(if (enabled) Modifier.pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))) else Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()
            text()
        }
    }
}