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
import com.gw2tb.manager.util.fileinfo.FileVersion
import org.apache.logging.log4j.LogManager
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

/**
 * A discoverer addonloader add-ons.
 *
 * This discoverer treats all unclaimed DLLs with a relative path of `addons/<addon-name>/gw2addon_<addon-name>.dll` as
 * add-ons.
 */
class LegacyAddOnDiscoverer : AddOnDiscoverer {

    private companion object {
        private val log = LogManager.getLogger(LegacyAddOnDiscoverer::class)
    }

    override fun AddOnDiscoveryContext.getAddOns(gameDirectory: Path): List<LocalAddOn> {
        val addOnsDirectory = gameDirectory.resolve("addons")

        if (!addOnsDirectory.isDirectory()) {
            log.info("Skipping legacy add-on discovery because 'addons' directory does not exist")
            return emptyList()
        }

        return addOnsDirectory.listDirectoryEntries()
            .filter(Path::isDirectory)
            .flatMap(::discover)
    }

    private fun discover(directory: Path): List<LocalAddOn> = buildList {
        val addonFileBaseName = "gw2addon_${directory.fileName}"

        val enabledAddOnPath = directory.resolve("$addonFileBaseName.dll")
        val disabledAddOnPath = directory.resolve("$addonFileBaseName.dll.disabled")

        if (enabledAddOnPath.isRegularFile()) {
            add(LocalAddOn(
                kind = LocalAddOn.Kind.ADDONLOADER_ADDON,
                path = enabledAddOnPath,
                name = directory.fileName.toString(),
                version = AddOnVersion(FileVersion(0u, 0u, 0u, 0u))
            ))
        }

        if (disabledAddOnPath.isRegularFile()) {
            add(LocalAddOn(
                kind = LocalAddOn.Kind.ADDONLOADER_ADDON,
                path = disabledAddOnPath,
                name = directory.fileName.toString(),
                version = AddOnVersion(FileVersion(0u, 0u, 0u, 0u))
            ))
        }
    }

}
