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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gw2tb.manager.ui.composables.AddOnList
import com.gw2tb.manager.ui.composables.AddOnListItem
import com.gw2tb.manager.ui.composables.AddOnListItemState
import com.gw2tb.manager.ui.theme.ManagerColors

@Composable
fun ManageAddOns(
    component: ManageComponent,
    modifier: Modifier = Modifier
) {
    val installedAddOns by component.installedAddOns.collectAsState()
    val availableUpdates by component.availableUpdates.collectAsState()

    val jobs by component.jobs.collectAsState()

    AddOnList(
        items = installedAddOns,
        onClick = { item -> component.navigateToDetails(item.localAddOn.ref) },
        modifier = modifier
            .background(brush = Brush.verticalGradient(listOf(Color.White, ManagerColors.BackgroundTint))),
        itemModifier = { item ->
            val availableAddOnUpdate = availableUpdates.find { it.addOnId == item.listing?.id }

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
    ) { (localAddOn, listing) ->
        val availableAddOnUpdate = availableUpdates.find { it.addOnId == listing?.id }

        AddOnListItem(
            title = listing?.addOnName ?: localAddOn.name,
            summary = listing?.addOnSummary ?: "",
            version = localAddOn.version.toString(),
            addOnState = if (localAddOn.isEnabled) AddOnListItemState.ENABLED else AddOnListItemState.DISABLED,
            updateAddOn = { component.updateAddOn(availableAddOnUpdate!!) },
            installAddOn = { error("Should never be reached") }, // In this screen, add-ons are already installed
            setAddOnEnabled = { enabled -> component.setEnabled(localAddOn.ref, enabled) },
            getJobs = { jobs.filter { localAddOn.ref in it.localAddOns } },
            availableAddOnUpdate = availableAddOnUpdate
        )
    }
}
