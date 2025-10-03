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
                        version = "",
                        addOnState = if (localAddOn.isEnabled) AddOnListItemState.ENABLED else AddOnListItemState.DISABLED,
                        onClick = { component.navigateToDetails(localAddOn.ref) },
                        updateAddOn = {}, // TODO
                        installAddOn = { error("Should never be reached") }, // In this screen, add-ons are already installed
                        setAddOnEnabled = { enabled -> component.setEnabled(localAddOn.ref, enabled) },
                        getJobs = { jobs.filter { localAddOn.ref in it.localAddOns } },
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

        // TODO There is a weird interaction here where the scrollbar picks up hover focus although it is not visible
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 2.dp),
            adapter = rememberScrollbarAdapter(lazyListState),
            style = LocalScrollbarStyle.current.copy(
                thickness = 6.dp,
                shape = RectangleShape
            )
        )
    }
}

/*
@Composable
fun ManageAddOnsDetails(
    component: ManageComponent,
    modifier: Modifier = Modifier
) {
    val addOnListings by component.addOnListings.collectAsState()
    val selectedLocalAddOn by component.selectedAddOn.collectAsState()
    val availableUpdates by component.availableUpdates.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (selectedLocalAddOn == null) {
            Text(
                text = stringResource(Res.string.no_addon_selected),
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black.copy(alpha = ContentAlpha.disabled)
            )
        } else {
            @Suppress("NAME_SHADOWING")
            val selectedLocalAddOn = selectedLocalAddOn!!

            val addOnListing = addOnListings.find { it isMatching selectedLocalAddOn }
            val availableAddOnUpdate = availableUpdates.find { it.localAddOn == selectedLocalAddOn }

            val jobs by component.jobs.collectAsState()

            AddOnDetails(
                addOnListing = addOnListing,
                localAddOn = selectedLocalAddOn,
                installAddOn = { error("Should never be reached") }, // In this screen, add-ons are already installed
                uninstallAddOn = { component.uninstall(selectedLocalAddOn) },
                updateAddOn = { component.install(addOnListing!!) },
                navigateToVendor = component::navigateToVendor,
                modifier = modifier
                    .fillMaxSize()
                    .padding(all = 8.dp),
                availableAddOnUpdate = availableAddOnUpdate,
                getJobs = { jobs.filter { selectedLocalAddOn in it.localAddOns } }
            )
        }
    }
}
 */