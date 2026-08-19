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
import com.gw2tb.manager.util.fileinfo.AddOnFileInfo
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * A discoverer for versions of ArcDps without support for GW2Load.
 *
 * This discoverer attempts to read [add-on file info][AddOnFileInfo] for all unclaimed DLLs in the directory tree and
 * decides based on the reported name whether the DLL is ArcDps.
 *
 * Versions of ArcDps with support for GW2Load will be discovered through the regular [Gw2LoadAddOnDiscoverer].
 */
class LegacyArcDpsDiscoverer : AddOnDiscoverer {

    private companion object {

        private val log = LogManager.getLogger(LegacyArcDpsDiscoverer::class)

    }

    override fun AddOnDiscoveryContext.getAddOns(gameDirectory: Path): List<LocalAddOn> {
        return buildList {
            Files.walkFileTree(gameDirectory, object : SimpleFileVisitor<Path>() {

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    add(file)
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                    log.warn("Skipping over subtree due to I/O exception: ${file.relativize(gameDirectory)}", exc)
                    return FileVisitResult.SKIP_SUBTREE
                }

            })
        }
            .filter(isDistinctFrom(discoveredAddOns))
            .mapNotNull { discover(it) }
            .toList()
    }

    private fun AddOnDiscoveryContext.discover(path: Path): LocalAddOn? {
        if (!path.toString().endsWith(".dll") && !path.toString().endsWith(".dll.disabled")) return null

        val addOnFileInfo = getAddOnFileInfo(path) ?: return null
        if (!addOnFileInfo.name.contentEquals("arcdps")) return null

        return LocalAddOn(
            kind = LocalAddOn.Kind.ARC_DPS,
            name = addOnFileInfo.name,
            path = path,
            version = AddOnVersion(addOnFileInfo.version, addOnFileInfo.versionString)
        )
    }

}
