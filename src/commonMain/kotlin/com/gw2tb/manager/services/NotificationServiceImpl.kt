package com.gw2tb.manager.services

import com.gw2tb.manager.model.Notification
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

fun NotificationService(
    addOnService: AddOnService,
    configurationService: ConfigurationService
): NotificationService = NotificationServiceImpl(
    addOnService = addOnService,
    configurationService = configurationService
)

class NotificationServiceImpl(
    configurationService: ConfigurationService,
    addOnService: AddOnService
) : NotificationService {

    override val notifications = addOnService.availableUpdates
        .mapNotNull { updates ->
            if (updates.isEmpty()) return@mapNotNull null

            listOf(
                Notification(
                    urgency = Notification.Urgency.INFO,
                    quickFix = {
                        updates.forEach { update ->
                            val localConfiguration = configurationService.localConfiguration.first()
                            val gameDirectory = localConfiguration?.selectedGameDirectory ?: error("Game directory should not be null")

                            addOnService.install(
                                listing = update.addOnListing,
                                gameDirectory = gameDirectory
                            )
                        }
                    }
                )
            )
        }

}