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
import com.gw2tb.manager.util.Maybe
import com.gw2tb.manager.util.fileinfo.AddOnFileInfo
import com.gw2tb.manager.util.fileinfo.readAddOnFileInfo
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

interface AddOnDiscoveryContext {

    val discoveredAddOns: Map<Path, LocalAddOn>

    fun getAddOnFileInfo(path: Path): AddOnFileInfo?

}

abstract class AbstractAddOnDiscoveryContext : AddOnDiscoveryContext {

    private val addOnFileInfoCache = ConcurrentHashMap<Path, Maybe<AddOnFileInfo>>()
    final override fun getAddOnFileInfo(path: Path): AddOnFileInfo? = addOnFileInfoCache.getOrPut(path) { Maybe(path.readAddOnFileInfo()) }.orNull

}

object EmptyAddOnDiscoveryContext : AbstractAddOnDiscoveryContext() {
    override val discoveredAddOns: Map<Path, LocalAddOn> get() = emptyMap()
}
