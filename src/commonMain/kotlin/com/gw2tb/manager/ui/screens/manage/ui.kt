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
package com.gw2tb.manager.ui.screens.manage

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.gw2tb.manager.ui.composables.AddOnListItem
import com.gw2tb.manager.ui.composables.AddOnListItemState

@Composable
fun ManageAddOns(
    component: ManageComponent,
    modifier: Modifier = Modifier
) {
    val availableUpdates by component.availableUpdates.collectAsState()

    val jobs by component.jobs.collectAsState()

    Box(
        modifier = modifier
            .background(brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFE6F6F6))))
    ) {
        val installedAddOns by component.installedAddOns.collectAsState()
        val lazyListState = rememberLazyListState()

        LazyColumn(
            state = lazyListState
        ) {
            itemsIndexed(items = installedAddOns) { index, (localAddOn, listing) ->
                val availableAddOnUpdate = availableUpdates.find { it.addOnId == listing?.id }

                Column(
                    modifier = Modifier
                        .let {
                            if (availableAddOnUpdate != null)
                                it.background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF9BD99F)),
                                        startX = 650F
                                    )
                                )
                            else
                                it
                        }
                ) {
                    AddOnListItem(
                        title = listing?.addOnName ?: localAddOn.name,
                        summary = listing?.addOnSummary ?: "",
                        version = localAddOn.version.toString(),
                        addOnState = if (localAddOn.isEnabled) AddOnListItemState.ENABLED else AddOnListItemState.DISABLED,
                        onClick = { component.navigateToDetails(localAddOn.ref) },
                        updateAddOn = { component.updateAddOn(availableAddOnUpdate!!) },
                        installAddOn = { error("Should never be reached") }, // In this screen, add-ons are already installed
                        setAddOnEnabled = { enabled -> component.setEnabled(localAddOn.ref, enabled) },
                        getJobs = { jobs.filter { localAddOn.ref in it.localAddOns } },
                        availableAddOnUpdate = availableAddOnUpdate,
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 18.dp)
                    )

                    if (index < installedAddOns.lastIndex)
                        Box(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .height((1f / LocalDensity.current.density).dp)
                                .background(
                                    brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, Color(0xFF8ad3d3), Color.Transparent))
                                )
                        ) {}
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
