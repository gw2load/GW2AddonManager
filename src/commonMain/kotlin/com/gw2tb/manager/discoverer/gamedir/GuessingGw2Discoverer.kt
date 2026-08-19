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
package com.gw2tb.manager.discoverer.gamedir

import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Resolves the Guild Wars 2 game directory by guessing the default installation path.
 *
 * By default, the Guild Wars 2 installation path is assumed to be `C:\Program Files\Guild Wars 2`.
 */
class GuessingGw2Discoverer(
    private val gw2ExecutablePath: Path = Path.of("C:\\Program Files\\Guild Wars 2\\Gw2-64.exe")
) : Gw2Discoverer {

    private companion object {
        private val log = LogManager.getLogger()
    }

    override fun findGameDirectory(): Path? {
        log.info("Inspecting default Guild Wars 2 installation path: {}", gw2ExecutablePath.absolutePathString())
        return if (Files.isRegularFile(gw2ExecutablePath)) gw2ExecutablePath.parent else null
    }

}