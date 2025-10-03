/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024 Leon Linhart
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
package com.gw2tb.manager.model.catalog

import androidx.compose.runtime.Immutable
import com.gw2tb.manager.model.AddOnId
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.local.AddOnVersion
import com.gw2tb.manager.model.local.LocalAddOn

infix fun AddOnListing.isMatching(localAddOn: LocalAddOn): Boolean =
    localAddOn.name in addOnNames

infix fun AddOnListing.isMatching(localAddOn: LocalAddOnReference): Boolean =
    localAddOn.name in addOnNames

/**
 * Represents an add-on listing from an add-on repository.
 *
 * @param id                a unique identifier for the add-on
 * @param addOnName         the (display) name of the add-on
 * @param addOnSummary      a short summary of the add-on's purpose
 * @param addOnDescription  a comprehensive description of the add-on
 *
 * @param homepageUrl       the URL to the add-on's homepage
 * @param issueTrackerUrl   the URL to the add-on's issue tracker
 * @param vcsUrl            the URL to the add-on's version control system
 *
 * @param vendorName        the name of the add-on's vendor
 * @param vendorUrl         the URL to the add-on vendor's homepage
 *
 * @param downloadUrl       the URL to download the add-on from
 * @param version           the add-on's latest version
 * @param addOnNames        a list of all names for the add-on that have been observed in `VERSIONINFO`. This
 *                          information can be used to match local add-ons with add-on listings.
 *
 * @param installMode       the mode in which the add-on should be installed
 * @param dependencies      a list of add-on names that this add-on depends on
 */
@Immutable
data class AddOnListing(
    val id: AddOnId,
    val addOnName: String,
    val addOnSummary: String,
    val addOnDescription: String,

    val homepageUrl: String? = null,
    val issueTrackerUrl: String? = null,
    val vcsUrl: String? = null,

    val vendorName: String,
    val vendorUrl: String? = null,

    val download: Download?,
    val addOnNames: List<String>,

    val installMode: InstallMode,
    val dependencies: List<AddOnId>
) {

    @Immutable
    data class Download(
        val downloadUrl: String,
        val version: AddOnVersion,
    )

    enum class InstallMode {
        Arc,
        Gw2Load
    }

}