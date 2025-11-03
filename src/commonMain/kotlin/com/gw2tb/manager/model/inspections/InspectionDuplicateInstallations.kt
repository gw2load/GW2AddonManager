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
package com.gw2tb.manager.model.inspections

import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.isMatching
import com.gw2tb.manager.model.local.LocalAddOn
import org.slf4j.LoggerFactory

/**
 * A duplicate installation of an add-on.
 *
 * @param id    the ID of the add-on
 * @param refs  the references to all local installations of the add-on
 */
data class InspectionDuplicateInstallations(
    val id: AddOnId,
    val refs: Iterable<LocalAddOnReference>
) : Inspection {

    companion object : Inspector<InspectionDuplicateInstallations> {

        private val log = LoggerFactory.getLogger(InspectionDuplicateInstallations::class.java)

        override fun InspectionContext.inspect(): Iterable<InspectionDuplicateInstallations> = buildList {
            val localAddOnsById = localAddOns
                .groupBy { localAddOn ->
                    val listing = addOnListings.find { listing -> listing isMatching localAddOn } ?: let {
                        log.debug("Skipping duplicate check for '{}' @ '{}' because add-on is not listed", localAddOn.name, localAddOn.path)
                        return@groupBy null
                    }

                    listing.id
                }

            for ((id, localAddOns) in localAddOnsById) {
                if (id == null) {
                    continue
                }

                if (localAddOns.size > 1) {
                    val refs = localAddOns.map(LocalAddOn::ref)
                    add(InspectionDuplicateInstallations(id, refs))
                }
            }
        }

    }

    override val affectedRefs: Iterable<LocalAddOnReference>
        get() = refs

}
