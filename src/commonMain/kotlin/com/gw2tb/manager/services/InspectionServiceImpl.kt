/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2025 Leon Linhart
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
package com.gw2tb.manager.services

import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.inspections.Inspection
import com.gw2tb.manager.model.inspections.InspectionAddOnUpdateAvailable
import com.gw2tb.manager.model.inspections.InspectionContext
import com.gw2tb.manager.model.inspections.InspectionDuplicateInstallations
import com.gw2tb.manager.model.inspections.InspectionMigrationPossible
import com.gw2tb.manager.model.inspections.InspectionMissingAddOnDependencies
import com.gw2tb.manager.model.inspections.Inspector
import com.gw2tb.manager.model.local.LocalAddOn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.coroutines.CoroutineContext

fun InspectionService(
    addOnService: AddOnService,
    mainContext: CoroutineContext
): InspectionService = InspectionServiceImpl(
    addOnService,
    mainContext
)

class InspectionServiceImpl(
    addOnService: AddOnService,
    mainContext: CoroutineContext
) : InspectionService {

    private val coroutineScope = CoroutineScope(mainContext + SupervisorJob())

    override val inspections: Flow<Map<Inspector<*>, Iterable<Inspection>>> =
        addOnService.addOnListings
            .combine(addOnService.localAddOns) { a, b -> a to b }
            .combine(addOnService.allLocalAddOns) { (addOnListings, localAddOns), allLocalAddOns -> inspect(addOnListings, allLocalAddOns, localAddOns) }
            .shareIn(coroutineScope, SharingStarted.Lazily, replay = 1)

    private fun inspect(
        listings: Iterable<AddOnListing>,
        allLocalAddOns: Iterable<LocalAddOn>,
        localAddOns: Iterable<LocalAddOn>
    ): Map<Inspector<*>, Iterable<Inspection>> {
        val inspectors = listOf(
            InspectionDuplicateInstallations,
            InspectionMissingAddOnDependencies,
            InspectionMigrationPossible,
            InspectionAddOnUpdateAvailable,
        )

        return buildMap inspections@{
            val inspectionContext = object : InspectionContext {

                override val addOnListings: Iterable<AddOnListing> get() = listings
                override val allLocalAddOns: Iterable<LocalAddOn> get() = allLocalAddOns
                override val localAddOns: Iterable<LocalAddOn> get() = localAddOns

                override fun LocalAddOnReference.hasInspection(inspector: Inspector<*>): Boolean =
                    this@inspections[inspector]?.any { inspection -> this in inspection.affectedRefs } ?: false

                override fun hasInspector(inspector: Inspector<*>): Boolean = inspector in inspectors

            }

            for (inspector in inspectors) {
                val inspections = with(inspector) {
                    inspectionContext.inspect()
                }

                put(inspector, inspections)
            }
        }
    }

}
