package com.gw2tb.manager.ui.screens.manage

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
fun ManageAddOnsMaster(
    component: ManageComponent,
    modifier: Modifier = Modifier
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
            items(items = localAddOns) { localAddOn ->
                val listing = addOnListings.find { it isMatching localAddOn }

                AddOnListItem(
                    title = listing?.addOnName ?: localAddOn.name,
                    addOnState = if (localAddOn.isEnabled) AddOnListItemState.ENABLED else AddOnListItemState.DISABLED,
                    selected = false,
                    onClick = { component.selectAddOn(localAddOn) },
                    installAddOn = { error("Should never be reached") }, // In this screen, add-ons are already installed
                    setAddOnEnabled = { enabled -> component.setEnabled(localAddOn, enabled) },
                    getJobs = { jobs.filter { localAddOn in it.localAddOns } },
                    showUpdateIndicator = availableUpdates.any { it.localAddOn == localAddOn },
                    contentPadding = PaddingValues(start = 8.dp, top = 2.dp, bottom = 2.dp, end = 10.dp)
                )
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