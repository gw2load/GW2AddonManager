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
package com.gw2tb.manager.ui.screens.explore

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.no_addon_selected
import com.gw2tb.manager.model.catalog.isMatching
import com.gw2tb.manager.ui.composables.AddOnDetails
import com.gw2tb.manager.ui.composables.AddOnListItem
import com.gw2tb.manager.ui.composables.AddOnListItemState
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExploreAddOnsMaster(
    component: ExploreComponent,
    modifier: Modifier = Modifier,
) {
    val addOnListings by component.addOnListings.collectAsState()
    val localAddOns by component.localAddOns.collectAsState()
    val availableUpdates by component.availableUpdates.collectAsState()

    val jobs by component.jobs.collectAsState()

    Box(
        modifier = modifier
    ) {
        val lazyListState = rememberLazyListState()

        LazyColumn(
            state = lazyListState
        ) {
            items(items = addOnListings) { listing ->
                val localAddOn = localAddOns.find { listing isMatching it }

                AddOnListItem(
                    title = listing.addOnName,
                    addOnState = when {
                        localAddOn == null -> AddOnListItemState.NOT_INSTALLED
                        localAddOn.isEnabled -> AddOnListItemState.ENABLED
                        else -> AddOnListItemState.DISABLED
                    },
                    selected = false,
                    onClick = { component.selectAddOn(listing) },
                    installAddOn = { component.install(listing) },
                    setAddOnEnabled = { enabled -> component.setEnabled(localAddOn!!, enabled) },
                    getJobs = { jobs.filter { listing in it.addOnListings } },
                    showUpdateIndicator = availableUpdates.any { it.addOnListing == listing },
                    contentPadding = PaddingValues(start = 8.dp, top = 2.dp, bottom = 2.dp, end = 10.dp)
                )
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

@Composable
fun ExploreAddOnsDetails(
    component: ExploreComponent,
    modifier: Modifier = Modifier
) {
    val selectedAddOnListing by component.selectedAddOn.collectAsState()
    val localAddOns by component.localAddOns.collectAsState()
    val availableUpdates by component.availableUpdates.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (selectedAddOnListing == null) {
            Text(
                text = stringResource(Res.string.no_addon_selected),
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black.copy(alpha = ContentAlpha.disabled)
            )
        } else {
            @Suppress("NAME_SHADOWING")
            val selectedAddOnListing = selectedAddOnListing!!

            val localAddOn = localAddOns.find { selectedAddOnListing isMatching it }
            val availableAddOnUpdate = availableUpdates.find { it.addOnListing == selectedAddOnListing }

            val jobs by component.jobs.collectAsState()

            AddOnDetails(
                addOnListing = selectedAddOnListing,
                localAddOn = localAddOn,
                installAddOn = { component.install(selectedAddOnListing) },
                uninstallAddOn = { component.uninstall(localAddOn!!) },
                updateAddOn = { component.install(selectedAddOnListing) },
                navigateToVendor = component::navigateToVendor,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 8.dp),
                availableAddOnUpdate = availableAddOnUpdate,
                getJobs = { jobs.filter { selectedAddOnListing in it.addOnListings } }
            )
        }
    }
}