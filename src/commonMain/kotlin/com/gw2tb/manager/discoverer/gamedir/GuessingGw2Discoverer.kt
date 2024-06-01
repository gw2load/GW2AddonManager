package com.gw2tb.manager.discoverer.gamedir

import java.nio.file.Files
import java.nio.file.Path

/**
 * Resolves the Guild Wars 2 game directory by guessing the default installation path.
 *
 * By default, the Guild Wars 2 installation path is assumed to be `C:\Program Files\Guild Wars 2`.
 */
class GuessingGw2Discoverer : Gw2Discoverer {

    override fun findGameDirectory(): Path? {
        val gw2ExecutablePath = Path.of("C:\\Program Files\\Guild Wars 2\\Gw2-64.exe")
        return if (Files.isRegularFile(gw2ExecutablePath)) gw2ExecutablePath.parent else null
    }

}