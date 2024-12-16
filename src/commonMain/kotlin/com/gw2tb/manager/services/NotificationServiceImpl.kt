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
package com.gw2tb.manager.services

import com.gw2tb.manager.model.Notification
import kotlinx.coroutines.flow.*

fun NotificationService(
    addOnService: AddOnService,
    configurationService: ConfigurationService,
    updateService: UpdateService
): NotificationService = NotificationServiceImpl(
    addOnService = addOnService,
    configurationService = configurationService,
    updateService = updateService
)

private class NotificationServiceImpl(
    addOnService: AddOnService,
    configurationService: ConfigurationService,
    updateService: UpdateService
) : NotificationService {

    override val notifications: Flow<List<Notification>> = flow {
        emitAll(updateService.availableUpdate
            .mapNotNull { update ->
                if (update == null) return@mapNotNull null

                listOf(
                    Notification(
                        urgency = Notification.Urgency.REQUIRED,
                        quickFix = { updateService.install(update) }
                    )
                )
            }
        )

        emitAll(addOnService.availableUpdates
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
        )
    }

}