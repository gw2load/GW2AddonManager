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

import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.util.listDirectoryEntries
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSameFileAs

class ArcDpsAddOnDiscoverer(
    private val blacklistedFiles: Set<Path> = EXCLUDED_GAME_FILES
) : AddOnDiscoverer {

    private companion object {

        /*
         * ArcDPS scans in the root game directory and ./bin64, so we need to maintain a blacklist of game files that
         * should not be considered as ArcDPS add-ons.
         */

        private val EXCLUDED_GAME_FILES = setOf(
            Path.of("bin64/cef"),
            Path.of("bin64/CoherentUI64.dll"),
            Path.of("bin64/d3dcompiler_43.dll"),
            Path.of("bin64/d3dcompiler_46.dll"),
            Path.of("bin64/dxgi.dll"),
            Path.of("bin64/ffmpegsumo.dll"),
            Path.of("bin64/icudt.dll"),
            Path.of("bin64/libEGL.dll"),
            Path.of("bin64/libGLESv2.dll"),
            Path.of("bin64/pdf.dll"),
            Path.of("d3d11.dll"),
            Path.of("dxgi.dll")
        )

    }

    override fun AddOnDiscoveryContext.getAddOns(gameDirectory: Path): List<LocalAddOn> {
        val arcDpsInstallations = discoveredAddOns.filter { (_, localAddOn) -> localAddOn.kind == LocalAddOn.Kind.ARC_DPS }

        val arcDpsDirectoryEntries = arcDpsInstallations
            .flatMap { (_, localAddOn) ->
                val installationDirectory = localAddOn.path.parent

                installationDirectory.listDirectoryEntries(IS_POTENTIAL_ADDON_FILE)
                    .filter(blacklistFilter(gameDirectory))
                    .filter { path -> discoveredAddOns.none { (_, localAddOn) -> localAddOn.path.isSameFileAs(gameDirectory.resolve(path)) } }
                    .mapNotNull(::discover)
            }

        val rootDirectoryEntries = gameDirectory.listDirectoryEntries(IS_POTENTIAL_ADDON_FILE)
                .filter(blacklistFilter(gameDirectory))
                .filter { path -> discoveredAddOns.none { (_, localAddOn) -> localAddOn.path.isSameFileAs(gameDirectory.resolve(path)) } }
                .mapNotNull(::discover)

        val bin64Directory = gameDirectory.resolve("bin64")
        val bin64DirectoryEntries = if (bin64Directory.isDirectory()) {
            bin64Directory.listDirectoryEntries(IS_POTENTIAL_ADDON_FILE)
                .filter(blacklistFilter(gameDirectory))
                .filter { path -> discoveredAddOns.none { (_, localAddOn) -> localAddOn.path.isSameFileAs(gameDirectory.resolve(path)) } }
                .mapNotNull(::discover)
        } else {
            emptyList()
        }

        return arcDpsDirectoryEntries + rootDirectoryEntries + bin64DirectoryEntries
    }

    private fun blacklistFilter(gameDirectory: Path): (Path) -> Boolean = { path ->
        blacklistedFiles.none { blacklistedFile ->
            val resolvedBlacklistedFile = gameDirectory.resolve(blacklistedFile)
            resolvedBlacklistedFile.isRegularFile() && path.isSameFileAs(resolvedBlacklistedFile)
        }
    }

    private fun discover(path: Path): LocalAddOn? {
        return LocalAddOn(
            path = path,
            kind = LocalAddOn.Kind.ARC_DPS_ADDON
        )
    }

}
