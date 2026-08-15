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

import com.gw2tb.manager.TestData
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.util.fileinfo.AddOnFileInfo
import com.gw2tb.manager.util.setupGameDirectory
import io.github.scordio.jimfs.junit.jupiter.JimfsTempDir
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.from
import org.junit.jupiter.api.Test
import java.nio.file.Path

class LegacyAddOnDiscovererTest {

    private val discoverer = LegacyAddOnDiscoverer()

    @Test
    fun `add-ons are found`(@JimfsTempDir gameDirectory: Path) {
        setupGameDirectory(gameDirectory) {
            dir("addons") {
                dir("FirstAddOn") {
                    addOn("gw2addon_FirstAddOn.dll")
                }
                dir("SecondAddOn") {
                    addOn("gw2addon_SecondAddOn.dll")
                }
                dir("ThirdAddOn") {
                    dir("gw2addon_SecondAddOn.dll") {}
                    dir("gw2addon_SecondAddOn.dll.disabled") {}
                }
            }
        }

        val context = object : AddOnDiscoveryContext {
            override val discoveredAddOns: Map<Path, LocalAddOn> = emptyMap()
            override fun getAddOnFileInfo(path: Path): AddOnFileInfo? = null
        }

        val localAddOns = with(discoverer) {
            context.getAddOns(gameDirectory)
        }

        assertThat(localAddOns)
            .hasSize(2)
            .first()
            .returns("FirstAddOn", from(LocalAddOn::name))
            .returns(gameDirectory.resolve("addons/FirstAddOn/gw2addon_FirstAddOn.dll"), from(LocalAddOn::path))
            .returns(LocalAddOn.Kind.ADDONLOADER_ADDON, from(LocalAddOn::kind))
    }

    @Test
    fun `addons directory does not exist`(@JimfsTempDir gameDirectory: Path) {
        setupGameDirectory(gameDirectory) {}

        val context = object : AddOnDiscoveryContext {
            override val discoveredAddOns: Map<Path, LocalAddOn> = emptyMap()
            override fun getAddOnFileInfo(path: Path): AddOnFileInfo? = null
        }

        val localAddOns = with(discoverer) {
            context.getAddOns(gameDirectory)
        }

        assertThat(localAddOns)
            .isEmpty()
    }

    @Test
    fun `disabled add-ons are found`(@JimfsTempDir gameDirectory: Path) {
        setupGameDirectory(gameDirectory) {
            dir("addons") {
                dir("FirstAddOn") {
                    addOn("gw2addon_FirstAddOn.dll")
                    addOn("gw2addon_FirstAddOn.dll.disabled")
                }
            }
        }

        val context = object : AddOnDiscoveryContext {
            override val discoveredAddOns: Map<Path, LocalAddOn> = emptyMap()
            override fun getAddOnFileInfo(path: Path): AddOnFileInfo? = null
        }

        val localAddOns = with(discoverer) {
            context.getAddOns(gameDirectory)
        }

        assertThat(localAddOns)
            .hasSize(2)
    }

    @Test
    fun `discovered add-ons are skipped`(@JimfsTempDir gameDirectory: Path) {
        setupGameDirectory(gameDirectory) {
            dir("addons") {
                dir("FirstAddOn") {
                    addOn("gw2addon_FirstAddOn.dll")
                    addOn("gw2addon_FirstAddOn.dll.disabled")
                }
            }
        }

        val context = object : AddOnDiscoveryContext {
            override val discoveredAddOns: Map<Path, LocalAddOn> = mapOf(
                gameDirectory.resolve("addons/FirstAddOn/gw2addon_FirstAddOn.dll")
                        to TestData.LocalAddOns.Apple1,
                gameDirectory.resolve("addons/FirstAddOn/gw2addon_FirstAddOn.dll.disabled")
                        to TestData.LocalAddOns.Apple1
            )
            override fun getAddOnFileInfo(path: Path): AddOnFileInfo? = null
        }

        val localAddOns = with(discoverer) {
            context.getAddOns(gameDirectory)
        }

        assertThat(localAddOns)
            .isEmpty()
    }

}
