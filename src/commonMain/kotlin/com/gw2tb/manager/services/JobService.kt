package com.gw2tb.manager.services

import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.local.LocalAddOn
import kotlinx.coroutines.flow.Flow

interface JobService {

    val jobs: Flow<List<Job>>

    suspend fun <T> runJob(
        addOnListings: List<AddOnListing> = emptyList(),
        localAddOns: List<LocalAddOn> = emptyList(),
        block: suspend () -> T
    )

}

class Job(
    val addOnListings: List<AddOnListing>,
    val localAddOns: List<LocalAddOn>
)