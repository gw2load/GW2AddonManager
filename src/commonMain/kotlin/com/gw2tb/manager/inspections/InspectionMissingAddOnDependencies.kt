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
package com.gw2tb.manager.inspections

import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.catalog.isMatching
import org.slf4j.LoggerFactory

/**
 * A duplicate installation.
 *
 * @param ref                   a reference to the local add-on installation
 * @param id                    the ID of the add-on
 * @param missingDependencies   the IDs of the missing dependencies
 */
data class InspectionMissingAddOnDependencies(
    val ref: LocalAddOnReference,
    val id: AddOnId,
    val disabledDependencies: Iterable<LocalAddOnReference>,
    val missingDependencies: Iterable<AddOnId>
) : Inspection {

    companion object : Inspector<InspectionMissingAddOnDependencies> {

        private val log = LoggerFactory.getLogger(InspectionMissingAddOnDependencies::class.java)

        override fun InspectionContext.inspect(): Iterable<InspectionMissingAddOnDependencies> = buildList {
            val addOnIdToListing = addOnListings.associateBy(AddOnListing::id)

            for (localAddOn in localAddOns) {
                if (localAddOn.ref.hasInspection(InspectionDuplicateInstallations)) {
                    log.debug("Skipping dependency check for '{}' because add-on has duplicate installations", localAddOn)
                    continue
                }

                val listing = addOnListings.find { listing -> listing isMatching localAddOn } ?: let {
                    log.debug("Skipping dependency check for '{}' because add-on is not listed", localAddOn.name)
                    continue
                }

                val allDependencies = findAllDependencies(listing.id, addOnIdToListing)

                val disabledDependencies = if (localAddOn.isEnabled) {
                    allDependencies
                        .mapNotNull { dependencyId ->
                            val dependencyListing = addOnListings.find { listing -> listing.id == dependencyId } ?: error("Unreachable state")
                            localAddOns.find { localAddOn -> dependencyListing isMatching localAddOn && !localAddOn.isEnabled }?.ref
                        }
                } else {
                    emptyList()
                }

                val missingDependencies = allDependencies
                    .filter { dependencyId ->
                        val dependencyListing = addOnListings.find { listing -> listing.id == dependencyId } ?: error("Unreachable state")
                        localAddOns.none { localAddOn -> dependencyListing isMatching localAddOn }
                    }

                if (disabledDependencies.isEmpty() && missingDependencies.isEmpty()) {
                    log.debug("All dependencies are satisfied for '{}' @ '{}'", listing.id, localAddOn.path)
                    continue
                }

                add(InspectionMissingAddOnDependencies(localAddOn.ref, listing.id, disabledDependencies, missingDependencies))
            }
        }

        // TODO Figure out a better place to put this as this is duplicated in the AddOnServiceImpl
        private fun findAllDependencies(id: AddOnId, addOnListings: Map<AddOnId, AddOnListing>): Set<AddOnId> {
            val queue = ArrayDeque<AddOnId>()
            queue += id

            val dependencies = mutableSetOf<AddOnId>()

            while (queue.isNotEmpty()) {
                val currentId = queue.removeFirst()
                val listing = addOnListings[currentId] ?: error("Could not find listing for add-on: $currentId")

                for (dependency in listing.dependencies) {
                    if (dependencies.add(dependency)) {
                        queue.add(dependency)
                    }
                }
            }

            return dependencies.toSet()
        }

    }

    override val affectedRefs: Iterable<LocalAddOnReference>
        get() = setOf(ref)

}
