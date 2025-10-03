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

import com.gw2tb.manager.model.AddOnId
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.isMatching
import org.slf4j.LoggerFactory

/**
 * An available update.
 *
 * @param update    the available update
 */
data class InspectionAddOnUpdateAvailable(
    val update: AvailableAddOnUpdate
) : Inspection {

    companion object : Inspector<InspectionAddOnUpdateAvailable> {

        private val log = LoggerFactory.getLogger(InspectionAddOnUpdateAvailable::class.java)

        override fun InspectionContext.inspect(): Iterable<InspectionAddOnUpdateAvailable> = buildList {
            for (listing in addOnListings) {
                if (listing.download == null) {
                    log.debug("Skipping update check for '{}' because no download is available", listing.id)
                    continue
                }

                val localAddOns = localAddOns.filter { localAddOn -> listing isMatching localAddOn }

                if (localAddOns.isEmpty()) {
                    log.debug("Skipping update check for '{}' because no installation was found", listing.id)
                    continue
                }

                if (hasInspector(InspectionDuplicateInstallations) && localAddOns.size > 1) {
                    /*
                     * If there is more than one installation, updating becomes a bit messy. Technically, the manager is
                     * capable of updating multiple installations independently. However, properly supporting this would
                     * require further UI work to disambiguate between installations. And even then, it is probably not
                     * worth it due to the amount of confusion this might cause.
                     *
                     * Instead, we usually let the inspection for duplicate installations handle this case and raise a
                     * warning. Only when the warning is resolved, the manager will actually check for updates.
                     */
                    log.warn("Skipping update check for '{}' because more than one installation was found", listing.id)
                    continue
                }

                val localAddOn = localAddOns.single()

                if (listing.download.version <= localAddOn.version) {
                    log.debug("No update available for '{}' @ '{}'", listing.id, localAddOn.path)
                    continue
                }

                val availableUpdate = AvailableAddOnUpdate(listing.id, localAddOn.ref)
                log.info("Found available update for '{}' @ '{}' (from '{}' to '{}')", listing.id, localAddOn.path, localAddOn.version, listing.download.version)
                add(InspectionAddOnUpdateAvailable(availableUpdate))
            }
        }

    }

    override val affectRefs: Iterable<LocalAddOnReference>
        get() = setOf(update.localRef)

}
