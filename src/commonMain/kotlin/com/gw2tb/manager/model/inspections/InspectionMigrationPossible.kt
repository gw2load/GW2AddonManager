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
package com.gw2tb.manager.model.inspections

import com.gw2tb.manager.model.inspections.migrations.Migration
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.inspections.migrations.AddOnLoaderAddOnToGw2LoadAddOnMigration
import com.gw2tb.manager.model.inspections.migrations.AddOnLoaderMigration
import com.gw2tb.manager.model.inspections.migrations.D3D9WrapperMigration
import com.gw2tb.manager.model.inspections.migrations.MigrationContext
import com.gw2tb.manager.model.inspections.migrations.Migrator
import com.gw2tb.manager.model.local.LocalAddOn

/**
 * One or more migrations are possible.
 *
 * @param migrations    the possible migrations
 */
class InspectionMigrationPossible(
    val migrations: Iterable<Migration>
) : Inspection {

    companion object : Inspector<InspectionMigrationPossible> {

        val migrators = listOf(
            AddOnLoaderAddOnToGw2LoadAddOnMigration,
            D3D9WrapperMigration,
            AddOnLoaderMigration
        )

        override fun InspectionContext.inspect(): Iterable<InspectionMigrationPossible> {
            val migrations = buildMap<Migrator<*>, Iterable<Migration>> migrations@{
                val migrationContext = object : MigrationContext {

                    override val addOnListings: Iterable<AddOnListing> get() = this@inspect.addOnListings
                    override val allLocalAddOns: Iterable<LocalAddOn> get() = this@inspect.allLocalAddOns
                    override val localAddOns: Iterable<LocalAddOn> get() = this@inspect.localAddOns

                    override fun LocalAddOnReference.hasMigration(migrator: Migrator<*>): Boolean =
                        this@migrations[migrator]?.any { migration -> this in migration.affectedRefs } ?: false

                    override fun hasMigrator(migrator: Migrator<*>): Boolean = migrator in migrators

                }

                for (migrator in migrators) {
                    val migrations = with(migrator) {
                        migrationContext.migrate()
                    }

                    put(migrator, migrations)
                }
            }
                .values
                .flatten()

            return if (migrations.isNotEmpty()) {
                listOf(InspectionMigrationPossible(migrations))
            } else {
                emptyList()
            }
        }

    }

    override val affectedRefs: Iterable<LocalAddOnReference>
        get() = migrations.flatMap { it.affectedRefs }

}
