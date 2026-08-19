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

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.gw2tb.manager.ui.theme.ManagerColors
import com.gw2tb.manager.ui.theme.glimmer
import com.gw2tb.manager.ui.theme.indication.IndicationAlpha

@Composable
fun <E> AddOnList(
    items: List<E>,
    onClick: (E) -> Unit,
    modifier: Modifier = Modifier,
    itemKey: ((Int, E) -> Any)? = null,
    itemModifier: (E) -> Modifier = { Modifier },
    itemContentPadding: PaddingValues = PaddingValues(0.dp),
    item: @Composable (E) -> Unit,
) {
    val items = remember(items) { items }

    Box(modifier = modifier) {
        val lazyListState = rememberLazyListState()

        LazyColumn(state = lazyListState) {
            itemsIndexed(items, itemKey) { index, item ->
                Column(
                    modifier = itemModifier(item)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = glimmer(
                                color = ManagerColors.Primary,
                                alpha = IndicationAlpha(
                                    pressedAlpha = 0.32f,
                                    focusedAlpha = 0.32f,
                                    draggedAlpha = 0.24f,
                                    hoveredAlpha = 0.16f
                                )
                            ),
                            onClick = { onClick(item) }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(itemContentPadding)
                    ) {
                        item(item)
                    }

                    if (index < items.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .height((1f / LocalDensity.current.density).dp)
                                .background(
                                    brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, ManagerColors.Primary, Color.Transparent))
                                )
                        ) {}
                    }
                }
            }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(lazyListState),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 2.dp),
            style = LocalScrollbarStyle.current.copy(
                thickness = 6.dp,
                shape = RectangleShape
            )
        )
    }
}
