/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2025 Leon Linhart
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
package com.gw2tb.manager.discoverer

import com.gw2tb.manager.model.local.AddOnVersion
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.util.fileinfo.readAddOnFileInfo
import com.gw2tb.manager.util.fileinfo.AddOnFileInfo
import java.nio.file.Path
import kotlin.io.path.walk

/**
 * A discoverer for versions of ArcDps without support for GW2Load.
 *
 * This discoverer attempts to read [add-on file info][AddOnFileInfo] for all unclaimed DLLs in the directory tree and
 * decides based on the reported name whether the DLL is ArcDps.
 *
 * Versions of ArcDps with support for GW2Load will be discovered through the regular [Gw2LoadAddOnDiscoverer].
 */
class LegacyArcDpsDiscoverer : AddOnDiscoverer {

    override fun getAddOns(gameDirectory: Path, discoveredAddOns: List<LocalAddOn>): List<LocalAddOn> {
        return gameDirectory.walk()
            .filter(isDistinctFrom(discoveredAddOns))
            .mapNotNull(::discover)
            .toList()
    }

    private fun discover(path: Path): LocalAddOn? {
        if (!path.toString().endsWith(".dll") && !path.toString().endsWith(".dll.disabled")) return null

        val addOnFileInfo = path.readAddOnFileInfo() ?: return null
        if (!addOnFileInfo.name.contentEquals("arcdps")) return null

        return LocalAddOn(
            kind = LocalAddOn.Kind.ARC_DPS,
            name = addOnFileInfo.name,
            path = path,
            version = AddOnVersion(addOnFileInfo.version, addOnFileInfo.versionString)
        )
    }

}
