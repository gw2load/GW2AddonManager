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
package com.gw2tb.manager.discoverer

import com.gw2tb.manager.model.local.AddOnVersion
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.util.fileinfo.readAddOnFileInfo
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class LegacyAddOnDiscoverer : AddOnDiscoverer {

    override fun getAddOns(gameDirectory: Path): List<LocalAddOn> = buildList {
        val addOnsDir = gameDirectory.resolve("addons")
        if (!addOnsDir.isDirectory()) return@buildList

        val arcDpsDir = gameDirectory.resolve("addons/arcdps")
        if (arcDpsDir.isDirectory()) {
            addAll(
                (arcDpsDir.listDirectoryEntries("*.dll") + arcDpsDir.listDirectoryEntries(glob = "*.dll.disabled"))
                    .mapNotNull { path ->
                        val addOnFileInfo = path.readAddOnFileInfo()
                        if (addOnFileInfo == null) {
                            return@mapNotNull null
                        }

                        LocalAddOn(
                            kind = LocalAddOn.Kind.ADDON_LOADER,
                            path = path,
                            name = addOnFileInfo.name,
                            version = AddOnVersion(fileVersion = addOnFileInfo.version, versionString = addOnFileInfo.versionString)
                        )
                    }
            )
        }

        addAll(
            addOnsDir.listDirectoryEntries()
                .filter { it.name != "arcdps" }
                .flatMap {
                    it.listDirectoryEntries("gw2addon_*.dll") + it.listDirectoryEntries("gw2addon_*.dll.disabled")
                }
                .mapNotNull { path ->
                    val addOnFileInfo = path.readAddOnFileInfo()
                    if (addOnFileInfo == null) {
                        return@mapNotNull null
                    }

                    LocalAddOn(
                        kind = LocalAddOn.Kind.ADDON_LOADER,
                        path = path,
                        name = addOnFileInfo.name,
                        version = AddOnVersion(fileVersion = addOnFileInfo.version, versionString = addOnFileInfo.versionString)
                    )
                }
        )
    }

}
