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

import com.gw2tb.manager.discoverer.loader.Loader
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.util.fileinfo.AddOnFileInfo
import io.github.scordio.jimfs.junit.jupiter.JimfsTempDir
import org.junit.jupiter.api.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import java.nio.file.Path

class Gw2LoadAddOnDiscovererTest {

    @Test
    fun testGetAddOns(@JimfsTempDir gameDirectory: Path) {
        val loader = mock<Loader>()
        val discoverer = Gw2LoadAddOnDiscoverer(loader)

        val context = object : AddOnDiscoveryContext {
            override val discoveredAddOns: Map<Path, LocalAddOn> = emptyMap()
            override fun getAddOnFileInfo(path: Path): AddOnFileInfo? = null
        }

        val inOrder = inOrder(loader)

        with(discoverer) {
            context.getAddOns(gameDirectory)
        }

        inOrder.verify(loader).getAddOns(gameDirectory, ".*\\.dll(\\.disabled)?")
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun testGetAddOns_CustomPattern(@JimfsTempDir gameDirectory: Path) {
        val loader = mock<Loader>()
        val discoverer = Gw2LoadAddOnDiscoverer(loader, pattern = "foobar")

        val context = object : AddOnDiscoveryContext {
            override val discoveredAddOns: Map<Path, LocalAddOn> = emptyMap()
            override fun getAddOnFileInfo(path: Path): AddOnFileInfo? = null
        }

        val inOrder = inOrder(loader)

        with(discoverer) {
            context.getAddOns(gameDirectory)
        }

        inOrder.verify(loader).getAddOns(gameDirectory, "foobar")
        inOrder.verifyNoMoreInteractions()
    }

}