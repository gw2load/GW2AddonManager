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

import com.gw2tb.manager.discoverer.loader.Loader
import com.gw2tb.manager.model.local.LocalAddOn
import org.apache.logging.log4j.LogManager
import java.nio.file.Path
import kotlin.io.path.isDirectory

/**
 * An add-on discoverer that uses GW2Load to discover add-ons.
 *
 * @param loader    the add-on loader
 * @param pattern   the pattern to match add-on files against. This defaults to `.*\.dll(\.disabled)?` to match any DLL
 *                  file, including add-ons disabled by the manager.
 */
class Gw2LoadAddOnDiscoverer(
    private val loader: Loader,
    private val pattern: String = ".*\\.dll(\\.disabled)?"
) : AddOnDiscoverer {

    private companion object {
        private val log = LogManager.getLogger(Gw2LoadAddOnDiscoverer::class)
    }

    override fun getAddOns(gameDirectory: Path): List<LocalAddOn> {
        val addOnsDirectory = gameDirectory.resolve("addons")
        if (!addOnsDirectory.isDirectory()) {
            log.info("Skipping add-on discovery using GW2Load because 'addons' directory does not exist")
            return emptyList()
        }

        val localAddOns = loader.getAddOns(addOnsDirectory, pattern)
        log.info("Discovered {} add-ons using GW2Load", localAddOns.size)

        return localAddOns
    }

}
