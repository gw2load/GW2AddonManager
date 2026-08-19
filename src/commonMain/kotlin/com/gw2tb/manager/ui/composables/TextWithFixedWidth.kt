/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2026 Leon Linhart
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

import androidx.compose.foundation.layout.Box
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TextWithFixedWeightWidth(
    weight: FontWeight?,
    maxWeight: FontWeight? = null,
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit
) {
    // Kinda cursed, but it works better than anything else I've tried
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(color = Color.Transparent, fontWeight = maxWeight)) {
            text()
        }

        CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontWeight = weight)) {
            text()
        }
    }
}
