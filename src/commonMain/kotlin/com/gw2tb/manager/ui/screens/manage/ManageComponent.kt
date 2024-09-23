package com.gw2tb.manager.ui.screens.manage

import androidx.compose.runtime.Immutable
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.services.Job
import kotlinx.coroutines.flow.StateFlow

@Immutable
interface ManageComponent {

    val addOnListings: StateFlow<List<AddOnListing>>

    val localAddOns: StateFlow<List<LocalAddOn>>

    val availableUpdates: StateFlow<List<AvailableAddOnUpdate>>

    val jobs: StateFlow<List<Job>>

    val selectedAddOn: StateFlow<LocalAddOn?>

    fun disable(addOn: LocalAddOn)

    fun enable(addOn: LocalAddOn)

    fun setEnabled(addOn: LocalAddOn, enabled: Boolean) =
        if (enabled) enable(addOn) else disable(addOn)

    fun install(listing: AddOnListing)

    fun uninstall(addOn: LocalAddOn)

    fun selectAddOn(localAddOn: LocalAddOn)

    fun navigateToVendor(vendor: String)

}