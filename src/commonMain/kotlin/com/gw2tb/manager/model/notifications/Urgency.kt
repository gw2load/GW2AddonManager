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

/** The urgency level of a notification. */
enum class Urgency {
    /** A critical notification that requires immediate user attention before nominal operation can continue. */
    Critical,
    /** An error notification that requires user attention but that does not prevent nominal operation. */
    Error,
    /** A warning notification that informs the user that something went wrong without blocking them from proceeding. */
    Warning,
    /** An informational notification that the user may act upon. */
    Informational
}
