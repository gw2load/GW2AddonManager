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
package com.gw2tb.manager.model

import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.local.LocalAddOn
import java.nio.file.Path
import kotlin.io.path.relativeTo

data class InstalledAddOn(
    val localAddOn: LocalAddOn,
    val listing: AddOnListing?
)

fun InstalledAddOn.toDisplayString(gameDirectory: Path?): String {
    fun maybeRelative(path: Path) = if (gameDirectory != null) path.relativeTo(gameDirectory) else path

    if (listing != null) return listing.addOnName
    return when (localAddOn.kind) {
        LocalAddOn.Kind.GW2_LOAD_ADDON -> localAddOn.name
        else -> "${localAddOn.name} (${maybeRelative(localAddOn.path)})"
    }
}
