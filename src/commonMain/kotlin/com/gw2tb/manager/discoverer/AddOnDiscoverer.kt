package com.gw2tb.manager.discoverer

import com.gw2tb.manager.model.LocalAddOn
import java.nio.file.Path

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
     * @param gameDirectory the game directory to search for add-ons
     *
     * @return  a list of local add-ons found in the given game directory
     */
    fun getAddOns(gameDirectory: Path): List<LocalAddOn>

}