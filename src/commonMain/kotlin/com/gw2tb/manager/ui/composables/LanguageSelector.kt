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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.select_language_label
import org.jetbrains.compose.resources.stringResource
import java.awt.Cursor
import java.util.Locale

@Composable
fun LanguageSelector(
    selectLocale: (locale: Locale) -> Unit
) {
    var isDropdownVisible by remember { mutableStateOf(false) }

    var width by remember { mutableIntStateOf(0) }
    val coordinates = remember { Ref<LayoutCoordinates>() }

    val color = lerp(Color(0xFF8ad3d3), Color.Black, 0.4F)

    Row(
        modifier = Modifier
            .onGloballyPositioned {
                width = it.size.width
                coordinates.value = it
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { isDropdownVisible = !isDropdownVisible }
            )
            .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            .border(1.dp, color = color)
    ) {
        Row(
            modifier = Modifier
                .background(brush = Brush.verticalGradient(listOf(Color.White, Color(0xFF8ad3d3))))
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            Text(
                text = stringResource(Res.string.select_language_label),
                color = color,
                fontSize = 10.sp
            )
        }

        Row(
            modifier = Modifier
                .background(brush = Brush.verticalGradient(listOf(lerp(Color(0xFF8ad3d3), Color.Black, 0.4F), Color(0xFF8ad3d3))))
                .padding(4.dp),
        ) {
            Text(
                text = LocalAppLocaleIso.current.language.uppercase(),
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
                    .width(width.dp)
                    .background(Color.White, shape = RectangleShape)
                    .border(1.dp, color = lerp(Color(0xFF8ad3d3), Color.Black, 0.4F)),
                properties = @OptIn(ExperimentalComposeUiApi::class) PopupProperties(
                    usePlatformInsets = false
                )
            ) {
                Column(
                    modifier = Modifier
                        .background(color = Color.White, shape = RectangleShape)
                        .fillMaxWidth()
                ) {
                    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 10.sp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectLocale(Locale.ENGLISH)
                                    isDropdownVisible = false
                                }
                                .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(text = "EN")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectLocale(Locale.GERMAN)
                                    isDropdownVisible = false
                                }
                                .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(text = "DE")
                        }
                    }
                }
            }
        }
    }
}

object LocalAppLocaleIso {

    private var default: Locale? = null
    private val LocalAppLocaleIso = staticCompositionLocalOf { Locale.getDefault() }
    val current: Locale
        @Composable get() = LocalAppLocaleIso.current

    @Composable
    infix fun provides(value: Locale?): ProvidedValue<*> {
        if (default == null) {
            default = Locale.getDefault()
        }
        val new = when(value) {
            null -> default!!
            else -> value
        }
        Locale.setDefault(new)
        return LocalAppLocaleIso.provides(new)
    }

}