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
package com.gw2tb.manager.discoverer.gamedir

import io.ktor.utils.io.errors.*
import java.nio.file.Files
import java.nio.file.Path

/**
 * Resolves the Guild Wars 2 game directory by inspecting the Steam library catalog found at Steam installation path.
 *
 * By default, the Steam installation path is assumed to be `C:\Program Files (x86)\Steam`.
 *
 * @param steamInstallationPath the path to the Steam installation directory
 */
class SteamGw2Discoverer(
    private val steamInstallationPath: Path = Path.of("C:\\Program Files (x86)\\Steam")
) : Gw2Discoverer {

    private companion object {

        private const val GW2_STEAM_ID = 1284210

        private val REGEX = """
                            |"path"\s*?"(.*?)"[^}]*?"apps"\s*\{[^{}]*?"$GW2_STEAM_ID"\h*"
                            """.trimMargin().toRegex()

    }

    override fun findGameDirectory(): Path? {
        val libraryFoldersVdfPath = steamInstallationPath.resolve("steamapps\\libraryfolders.vdf")

        try {
            if (!Files.isRegularFile(libraryFoldersVdfPath)) {
                return null
            }

            val libraryFoldersVdf = libraryFoldersVdfPath.toFile().readText()

            val gameDirectory = REGEX.find(libraryFoldersVdf)?.groupValues?.get(1)?.let(Path::of)?.resolve("steamapps\\common\\Guild Wars 2")
            return if (gameDirectory != null && Files.isRegularFile(gameDirectory.resolve("Gw2-64.exe"))) {
                gameDirectory
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace() // TODO proper logging
            return null
        }
    }

}