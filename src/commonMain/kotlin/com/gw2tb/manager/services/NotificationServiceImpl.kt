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