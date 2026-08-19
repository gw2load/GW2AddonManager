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
package com.gw2tb.manager.discoverer

import com.gw2tb.manager.model.local.AddOnVersion
import com.gw2tb.manager.model.local.LocalAddOn
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/**
 * An add-on discoverer that discovers GW2Load itself.
 *
 * @param libraryName   the (file) name of the GW2Load shared library
 */
class Gw2LoadDiscoverer(
    private val libraryName: String = "msimg32.dll",
    private val addOnName: String = "GW2Load"
) : AddOnDiscoverer {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(Gw2LoadDiscoverer::class.java)
    }

    override fun AddOnDiscoveryContext.getAddOns(gameDirectory: Path): List<LocalAddOn> {
        require(discoveredAddOns.isEmpty()) { "${this::class.simpleName} should always run first" }
        log.info("Searching directory for {} using {}: {}", addOnName, this::class.simpleName, gameDirectory)

        val libraryPath = gameDirectory.resolve(libraryName)
        val disabledLibraryPath = gameDirectory.resolve("$libraryName.disabled")

        return buildList {
            if (libraryPath.isRegularFile()) {
                log.info("Found {} at '{}'", addOnName, libraryPath)

                val addOnFileInfo = getAddOnFileInfo(libraryPath)
                if (addOnFileInfo != null) {
                    add(LocalAddOn(
                        kind = LocalAddOn.Kind.GW2_LOAD_LOADER,
                        name = addOnName,
                        path = libraryPath,
                        version = AddOnVersion(addOnFileInfo.version, addOnFileInfo.versionString),
                        isEnabled = true
                    ))
                }
            }

            if (disabledLibraryPath.isRegularFile()) {
                log.info("Found {} at '{}'", addOnName, libraryPath)

                val addOnFileInfo = getAddOnFileInfo(libraryPath)
                if (addOnFileInfo != null) {
                    add(LocalAddOn(
                        kind = LocalAddOn.Kind.GW2_LOAD_LOADER,
                        name = addOnName,
                        path = disabledLibraryPath,
                        version = AddOnVersion(addOnFileInfo.version, addOnFileInfo.versionString),
                        isEnabled = false
                    ))
                }
            }
        }
    }

}
