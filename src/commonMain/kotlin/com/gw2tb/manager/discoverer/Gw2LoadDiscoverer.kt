/*
 * GW2AddOnManager
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

    override fun getAddOns(gameDirectory: Path): List<LocalAddOn> {
        val libraryPath = gameDirectory.resolve(libraryName)
        val disabledLibraryPath = gameDirectory.resolve("$libraryName.disabled")

        return buildList {
            if (libraryPath.isRegularFile()) {
                add(LocalAddOn(
                    kind = LocalAddOn.Kind.GW2_LOAD_LOADER,
                    name = addOnName,
                    path = libraryPath,
                    version = libraryPath.getAddOnInfo()!!.version,
                    isEnabled = true
                ))
            }

            if (disabledLibraryPath.isRegularFile()) {
                add(LocalAddOn(
                    kind = LocalAddOn.Kind.GW2_LOAD_LOADER,
                    name = addOnName,
                    path = disabledLibraryPath,
                    version = libraryPath.getAddOnInfo()!!.version,
                    isEnabled = false
                ))
            }
        }
    }

}