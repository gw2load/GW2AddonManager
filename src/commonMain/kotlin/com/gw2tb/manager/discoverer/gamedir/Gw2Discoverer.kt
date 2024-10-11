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