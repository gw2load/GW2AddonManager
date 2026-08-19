/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2026 Leon Linhart
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
package com.gw2tb.manager.model.notifications

import com.gw2tb.manager.exceptions.ManagerException
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.ManagerVersion
import com.gw2tb.manager.inspections.InspectionDuplicateInstallations
import com.gw2tb.manager.inspections.InspectionMissingAddOnDependencies
import com.gw2tb.manager.inspections.migrations.Migration

/**
 * Indicates that add-on updates are available.
 *
 * @param updates   the available updates
 */
data class NotificationAddOnUpdatesAvailable(val updates: Iterable<AvailableAddOnUpdate>) : Notification {
    override val urgency: Urgency get() = Urgency.Informational
}

/**
 * Indicates that one or more add-ons have been installed multiple times.
 *
 * @param inspections   the list of inspections
 */
data class NotificationDuplicateInstallation(val inspections: Iterable<InspectionDuplicateInstallations>) : Notification {
    override val urgency: Urgency get() = Urgency.Critical
}

/**
 * Indicates that an exception occurred.
 *
 * @param exception the exception
 */
data class NotificationException(val exception: ManagerException) : Notification {
    override val urgency: Urgency get() = exception.urgency
}

/**
 * Indicates that an update for the manager is available.
 *
 * @param version   the latest available manager version
 */
data class NotificationManagerUpdateAvailable(val version: ManagerVersion) : Notification {
    override val urgency: Urgency get() = Urgency.Informational
}

/**
 * Indicates that migrations are possible.
 *
 * @param migrations    the possible migrations
 */
data class NotificationMigrationPossible(val migrations: Iterable<Migration>) : Notification {
    override val urgency: Urgency get() = Urgency.Informational
}

/**
 * Indicates that add-on dependencies are missing.
 *
 * @param inspections   the list of inspections
 */
data class NotificationMissingAddOnDependencies(val inspections: Iterable<InspectionMissingAddOnDependencies>) : Notification {
    override val urgency: Urgency get() = Urgency.Critical
}
