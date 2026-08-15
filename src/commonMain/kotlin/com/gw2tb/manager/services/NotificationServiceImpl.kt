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

import com.gw2tb.manager.inspections.InspectionAddOnUpdateAvailable
import com.gw2tb.manager.inspections.InspectionDuplicateInstallations
import com.gw2tb.manager.inspections.InspectionMigrationPossible
import com.gw2tb.manager.inspections.InspectionMissingAddOnDependencies
import com.gw2tb.manager.model.notifications.NotificationAddOnUpdatesAvailable
import com.gw2tb.manager.model.notifications.NotificationDuplicateInstallation
import com.gw2tb.manager.model.notifications.NotificationException
import com.gw2tb.manager.model.notifications.NotificationManagerUpdateAvailable
import com.gw2tb.manager.model.notifications.NotificationMigrationPossible
import com.gw2tb.manager.model.notifications.NotificationMissingAddOnDependencies
import kotlinx.coroutines.flow.combine

fun NotificationService(
    exceptionService: ExceptionService,
    inspectionService: InspectionService,
    updateService: UpdateService
): NotificationService = NotificationServiceImpl(
    exceptionService = exceptionService,
    inspectionService = inspectionService,
    updateService = updateService
)

class NotificationServiceImpl(
    exceptionService: ExceptionService,
    inspectionService: InspectionService,
    updateService: UpdateService
) : NotificationService {

    override val notifications =
        combine(exceptionService.exceptions, inspectionService.inspections, updateService.availableUpdate) { exceptions, inspections, availableManagerUpdate ->
            buildList {
                for (exception in exceptions) {
                    add(NotificationException(exception))
                }

                for ((inspector, inspections) in inspections) {
                    when (inspector) {
                        InspectionAddOnUpdateAvailable -> {
                            val updates = inspections.map { (it as InspectionAddOnUpdateAvailable).update }
                            if (updates.isNotEmpty()) add(NotificationAddOnUpdatesAvailable(updates))
                        }
                        InspectionDuplicateInstallations -> {
                            val inspections = inspections.map { it as InspectionDuplicateInstallations }
                            if (inspections.isNotEmpty()) add(NotificationDuplicateInstallation(inspections))
                        }
                        InspectionMigrationPossible -> {
                            val inspections = inspections.map { it as InspectionMigrationPossible }
                            if (inspections.isNotEmpty()) add(NotificationMigrationPossible(inspections.single().migrations))
                        }
                        InspectionMissingAddOnDependencies -> {
                            val inspections = inspections.map { it as InspectionMissingAddOnDependencies }
                            if (inspections.isNotEmpty()) add(NotificationMissingAddOnDependencies(inspections))
                        }
                    }
                }

                if (availableManagerUpdate != null) {
                    add(NotificationManagerUpdateAvailable(availableManagerUpdate))
                }
            }
        }

}
