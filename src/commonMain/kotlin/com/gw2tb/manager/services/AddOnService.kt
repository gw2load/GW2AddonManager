package com.gw2tb.manager.services

import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.local.LocalAddOn
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

interface AddOnService {

    val addOnListings: Flow<List<AddOnListing>>

    val localAddOns: Flow<List<LocalAddOn>>

    val availableUpdates: Flow<List<AvailableAddOnUpdate>>

    suspend fun disable(addOn: LocalAddOn)

    suspend fun enable(addOn: LocalAddOn)

    suspend fun install(listing: AddOnListing, gameDirectory: Path)

    suspend fun uninstall(addOn: LocalAddOn)

    suspend fun refreshListings()

}