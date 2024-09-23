package com.gw2tb.manager.model

import androidx.compose.runtime.Immutable

@Immutable
data class Notification(
    val urgency: Urgency,
    val quickFix: (suspend () -> Unit)? = null
) {

    enum class Urgency {
        INFO,
        WARNING,
        REQUIRED
    }

}