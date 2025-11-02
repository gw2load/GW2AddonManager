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
package com.gw2tb.manager.ui.screens.explore

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gw2tb.manager.model.catalog.isMatching
import com.gw2tb.manager.ui.composables.AddOnList
import com.gw2tb.manager.ui.composables.AddOnListItem
import com.gw2tb.manager.ui.composables.AddOnListItemState
import com.gw2tb.manager.ui.theme.ManagerColors

@Composable
fun ExploreAddOns(
    component: ExploreComponent,
    modifier: Modifier = Modifier,
) {
    val addOnListings by component.addOnListings.collectAsState()
    val localAddOns by component.localAddOns.collectAsState()
    val availableUpdates by component.availableUpdates.collectAsState()

    val jobs by component.jobs.collectAsState()

    AddOnList(
        items = addOnListings,
        onClick = { item -> component.navigateToAddOnDetails(item.id) },
        modifier = modifier
            .background(brush = Brush.verticalGradient(listOf(Color.White, ManagerColors.BackgroundTint))),
        itemModifier = { item ->
            val availableAddOnUpdate = availableUpdates.find { it.addOnId == item.id }

            Modifier
                .let {
                    if (availableAddOnUpdate != null)
                        it.background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, ManagerColors.PositiveHint),
                                startX = 650F
                            )
                        )
                    else
                        it
                }
        },
        itemContentPadding = PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 18.dp)
    ) { item ->
        val localAddOn = localAddOns.find { item isMatching it }
        val availableAddOnUpdate = availableUpdates.find { it.addOnId == item.id }

        AddOnListItem(
            title = item.addOnName,
            summary = item.addOnSummary,
            version = item.download!!.version.toString(),
            addOnState = when {
                localAddOn == null -> AddOnListItemState.NOT_INSTALLED
                localAddOn.isEnabled -> AddOnListItemState.ENABLED
                else -> AddOnListItemState.DISABLED
            },
            updateAddOn = { component.updateAddOn(availableAddOnUpdate!!) },
            installAddOn = { component.installAddOn(item.id) },
            setAddOnEnabled = { enabled -> component.setEnabled(localAddOn!!.ref, enabled) },
            getJobs = { jobs.filter { item.id in it.addOnListings } },
            availableAddOnUpdate = availableAddOnUpdate
        )
    }
}
