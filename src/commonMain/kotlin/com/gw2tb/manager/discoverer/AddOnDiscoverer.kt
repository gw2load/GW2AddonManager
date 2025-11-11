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

import com.gw2tb.manager.model.local.LocalAddOn
import java.nio.file.Path
import kotlin.io.path.isSameFileAs

/**
 * An add-on discoverer is responsible for the discovery and inspection of local add-ons.
 *
 * The sole purpose of this interface is to provide a way to read immutable state from the file system. This is the sole
 * source of truth for the application about the locally installed add-ons.
 */
interface AddOnDiscoverer {

    /**
     * Returns a list of local add-ons found in the given game directory.
     *
     * @param gameDirectory     the game directory to search for add-ons
     * @param discoveredAddOns  the list of add-ons that has already been discovered (typically, by other discoverers
     *                          earlier in the chain)
     *
     * @return  a list of local add-ons found in the given game directory
     */
    fun getAddOns(gameDirectory: Path, discoveredAddOns: List<LocalAddOn>): List<LocalAddOn>

    /**
     * Returns a filtering function that checks whether a given path refers to the same file as a previously discovered
     * add-on.
     *
     * @param discoveredAddOns  the previously discovered add-ons to consider
     *
     * @return  the filter function
     */
    fun isDistinctFrom(discoveredAddOns: List<LocalAddOn>): (Path) -> Boolean = { path ->
        discoveredAddOns.none { localAddOn -> localAddOn.path.isSameFileAs(path) }
    }

}
