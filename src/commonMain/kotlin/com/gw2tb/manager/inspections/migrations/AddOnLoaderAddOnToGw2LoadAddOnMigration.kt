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
package com.gw2tb.manager.inspections.migrations

import com.gw2tb.manager.actions.Action
import com.gw2tb.manager.actions.ActionRenameAddOn
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.local.LocalAddOn
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** A migration that renames GW2Load-compatible legacy addonloader add-ons to ensure they are loaded by GW2Load. */
class AddOnLoaderAddOnToGw2LoadAddOnMigration(
    val ref: LocalAddOnReference,
    val newFileName: String
) : Migration {

    companion object : Migrator<AddOnLoaderAddOnToGw2LoadAddOnMigration> {

        val log: Logger = LoggerFactory.getLogger(AddOnLoaderAddOnToGw2LoadAddOnMigration::class.java)

        override fun MigrationContext.migrate(): Iterable<AddOnLoaderAddOnToGw2LoadAddOnMigration> {
            val addOnLoaderAddOns = allLocalAddOns.filter { localAddOn -> localAddOn.kind == LocalAddOn.Kind.ADDONLOADER_ADDON }
            if (addOnLoaderAddOns.isEmpty()) {
                log.debug("No addonloader add-ons found")
                return emptyList()
            }

            return addOnLoaderAddOns
                .mapNotNull { addOnLoaderAddOn -> localAddOns.find { it.path == addOnLoaderAddOn.path && it.kind == LocalAddOn.Kind.GW2_LOAD_ADDON } }
                .map { effectiveLocalAddOn ->
                    val newFileName = effectiveLocalAddOn.path.fileName.toString().removePrefix("gw2addon_")
                    AddOnLoaderAddOnToGw2LoadAddOnMigration(effectiveLocalAddOn.ref, newFileName)
                }
        }

    }

    override val affectedRefs: List<LocalAddOnReference>
        get() = listOf(ref)

    override fun migrate(): Iterable<Action> =
        listOf(ActionRenameAddOn(ref, newFileName))

}
