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

import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.platform.win32.getAddOnInfo
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class LegacyAddOnDiscoverer : AddOnDiscoverer {

    override fun getAddOns(gameDirectory: Path): List<LocalAddOn> {
        return gameDirectory.resolve("addons/arcdps")
            .let {
                it.listDirectoryEntries(glob = "*.dll") + it.listDirectoryEntries(glob = "*.dll.disabled")
            }
            .mapNotNull { path ->
                val addOnInfo = path.getAddOnInfo()
                if (addOnInfo == null) {
                    println("Failed to get add-on info for $path")
                    return@mapNotNull null
                }

                LocalAddOn(
                    kind = LocalAddOn.Kind.ADDON_LOADER,
                    path = path.also { println("Arc $it - ${it.fileName}") },
                    name = addOnInfo.name,
                    version = addOnInfo.version
                )
            } + gameDirectory.resolve("addons")
                .listDirectoryEntries().filter { it.name != "arcdps" }
                .flatMap {
                    it.listDirectoryEntries("gw2addon_*.dll") + it.listDirectoryEntries("gw2addon_*.dll.disabled")
                }
            .mapNotNull { path ->
                val addOnInfo = path.getAddOnInfo()
                if (addOnInfo == null) {
                    println("Failed to get add-on info for $path")
                    return@mapNotNull null
                }

                LocalAddOn(
                    kind = LocalAddOn.Kind.ADDON_LOADER,
                    path = path,
                    name = addOnInfo.name,
                    version = addOnInfo.version
                )
            }
    }

}