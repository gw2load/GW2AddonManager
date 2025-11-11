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
package com.gw2tb.manager.discoverer.loader

import com.gw2tb.manager.model.local.AddOnVersion
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.util.fileinfo.readAddOnFileInfo
import java.lang.foreign.Arena
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class Loader(
    val localAddOn: LocalAddOn,
    val isBundled: Boolean
) : AutoCloseable {

    private var isClosed = false

    private val libraryArena = Arena.ofShared()
    private val gw2Load: Gw2Load

    init {
        try {
            gw2Load = Gw2Load(libraryArena, localAddOn.path)
        } catch (e: UnsatisfiedLinkError) {
            libraryArena.close()
            throw IllegalArgumentException("The GW2Load shared library could not be loaded.", e)
        }
    }

    private lateinit var localAddOns: List<LocalAddOn>

    override fun close() {
        isClosed = true
        libraryArena.close()
    }

    fun getAddOns(directory: Path, pattern: String): List<LocalAddOn> {
        if (isClosed) return localAddOns

        return gw2Load.GetAddonsInDirectory(directory.absolutePathString(), pattern)
            .mapNotNull {
                val addOnFileInfo = Path.of(it.path).readAddOnFileInfo() ?: return@mapNotNull null

                val kind = when {
                    addOnFileInfo.name.equals("arcdps", ignoreCase = true) -> LocalAddOn.Kind.ARC_DPS
                    else -> LocalAddOn.Kind.GW2_LOAD_ADDON
                }

                LocalAddOn(
                    kind = kind,
                    name = it.name,
                    path = Path.of(it.path),
                    version = AddOnVersion(fileVersion = addOnFileInfo.version, versionString = addOnFileInfo.versionString),
                    isEnabled = it.isEnabled
                )
            }
            .also { localAddOns = it }
    }

}
