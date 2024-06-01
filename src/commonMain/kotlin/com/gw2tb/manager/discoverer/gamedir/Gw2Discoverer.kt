package com.gw2tb.manager.discoverer.gamedir

import java.nio.file.Path

/**
 * A `Gw2Discoverer` is responsible for finding the local Guild Wars 2 game directory.
 */
interface Gw2Discoverer {

    /**
     * Finds the Guild Wars 2 game directory.
     *
     * @return the Guild Wars 2 game directory, or `null` if it could not be found
     */
    fun findGameDirectory(): Path?

}