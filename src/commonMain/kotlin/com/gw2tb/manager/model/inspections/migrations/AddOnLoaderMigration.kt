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

/** A migration that removes all traces of the legacy addonloader once it is no longer required. */
class AddOnLoaderMigration(
    override val affectedRefs: List<LocalAddOnReference>
) : Migration {

    companion object : Migrator<AddOnLoaderMigration> {

        val log: Logger = LoggerFactory.getLogger(AddOnLoaderMigration::class.java)

        override fun MigrationContext.migrate(): Iterable<AddOnLoaderMigration> {
            if (localAddOns.any { localAddOn -> localAddOn.kind == LocalAddOn.Kind.ADDONLOADER_ADDON }) {
                log.debug("Addonloader add-ons prevent addonloader removal")
                return emptyList()
            }

            val addOnLoader = localAddOns.filter { localAddOn -> localAddOn.kind == LocalAddOn.Kind.ADDONLOADER }
            if (addOnLoader.isEmpty()) {
                log.debug("Found no addonloader-related installations to remove")
                return emptyList()
            }

            return listOf(AddOnLoaderMigration(affectedRefs = addOnLoader.map { it.ref }))
        }

    }

    override fun migrate(): Iterable<Action> =
        affectedRefs.map(::ActionUninstallAddOn)

}
