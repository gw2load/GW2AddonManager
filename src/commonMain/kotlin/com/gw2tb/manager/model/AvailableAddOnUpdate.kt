package com.gw2tb.manager.model

import androidx.compose.runtime.Immutable

@Immutable
data class AvailableAddOnUpdate(
    val addOnListing: AddOnListing,
    val localAddOn: LocalAddOn,
    val urgency: Urgency
) {

    enum class Urgency {
        OPTIONAL,
        REQUIRED
    }

}