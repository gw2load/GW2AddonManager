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
package com.gw2tb.manager.model.inspections.migrations

import com.gw2tb.manager.actions.Action
import com.gw2tb.manager.actions.ActionUninstallAddOn
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.local.LocalAddOn
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

/**  A migration that removes d3d9_wrapper if no addonloader add-ons without migration path remain. */
class D3D9WrapperMigration(
    val d3d9Wrapper: LocalAddOnReference
) : Migration {

    companion object : Migrator<D3D9WrapperMigration> {

        val log: Logger = LoggerFactory.getLogger(D3D9WrapperMigration::class.java)

        override fun MigrationContext.migrate(): Iterable<D3D9WrapperMigration> {
            val d3d9WrapperAddOn = localAddOns.find { it.path.endsWith(Path.of("addons/d3d9_wrapper/gw2addon_d3d9_wrapper.dll")) }
            if (d3d9WrapperAddOn == null) {
                log.debug("d3d9 wrapper not found")
                return emptyList()
            }

            if (localAddOns.count { localAddOn ->
                localAddOn.kind == LocalAddOn.Kind.ADDONLOADER_ADDON
                    && !localAddOn.ref.hasMigration(AddOnLoaderAddOnToGw2LoadAddOnMigration)
            } > 1) {
                log.debug("Cannot remove d3d9 wrapper")
                return emptyList()
            }

            return listOf(D3D9WrapperMigration(d3d9WrapperAddOn.ref))
        }

    }

    override val affectedRefs: List<LocalAddOnReference>
        get() = listOf(d3d9Wrapper)

    override fun migrate(): Iterable<Action> =
        affectedRefs.map(::ActionUninstallAddOn)

}
