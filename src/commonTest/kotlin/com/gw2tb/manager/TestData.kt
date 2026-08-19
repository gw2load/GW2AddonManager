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
package com.gw2tb.manager

import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.catalog.AddOnListing.InstallMode
import com.gw2tb.manager.model.local.AddOnVersion
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.util.fileinfo.FileVersion
import java.nio.file.Path

object TestData {

    object LocalAddOns {

        val Apple1 = LocalAddOn(
            kind = LocalAddOn.Kind.ARC_DPS,
            name = "S1",
            path = Path.of(""),
            version = AddOnVersion(FileVersion(1u, 2u, 3u, 4u))
        )

        val Apple2 = LocalAddOn(
            kind = LocalAddOn.Kind.ARC_DPS,
            name = "S1",
            path = Path.of(""),
            version = AddOnVersion(FileVersion(1u, 2u, 3u, 4u))
        )

    }

    object Listings {

        val Apple = AddOnListing(
            id = AddOnId("apple"),
            addOnName = "Apple",
            addOnSummary = "A red-ish add-on",
            addOnDescription = "",
            vendorName = "GW2TB",
            download = null,
            addOnNames = listOf("S1"),
            installMode = InstallMode.Gw2Load,
            dependencies = emptyList()
        )

        val Banana = AddOnListing(
            id = AddOnId("apple"),
            addOnName = "Apple",
            addOnSummary = "A red-ish add-on",
            addOnDescription = "",
            vendorName = "GW2TB",
            download = null,
            addOnNames = emptyList(),
            installMode = InstallMode.Gw2Load,
            dependencies = emptyList()
        )

    }

}
