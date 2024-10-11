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
package com.gw2tb.manager.repository

import com.gw2tb.manager.repository.AddOnRepositoryManifest.AddOnEntry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal val json = Json {
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

internal fun parseAddOnManifest(str: String): List<AddOnEntry> {
    return json.decodeFromString<AddOnRepositoryManifest>(str)
        .addons
        .toList()
}

/**
 * The manifest of an add-on repository.
 *
 * The manifest is a JSON file that contains the...
 */
@Serializable
@JvmInline
value class AddOnRepositoryManifest(val addons: List<AddOnEntry>) {

    @Serializable
    data class AddOnEntry(
        val `package`: Package,
        val host: JsonObject, // We don't really care about this atm
        val installation: Installation,
        val release: Release? = null,
        @SerialName("addon_names")
        val addonNames: List<String>? = null
    ) {

        @Serializable
        data class Package(
            val id: String,
            val name: String,
            val description: String,
            val tooltip: String,
            val website: String,
            val developer: String,
            @SerialName("issue_tracker")
            val issueTracker: String,
            val vcs: String? = null,
            val dependencies: List<String>? = null
        )

        @Serializable
        data class Installation(
            val mode: Mode
        ) {

            enum class Mode {
                @SerialName("arc")
                ARC,
                @SerialName("gw2load")
                GW2LOAD
            }

        }

        @Serializable
        data class Release(
            val id: String,
            val name: String,
            val version: List<Int>,
            @SerialName("version_str")
            val versionString: String,
            @SerialName("download_url")
            val downloadUrl: String,
            @SerialName("asset_index")
            val assetIndex: Int? = null
        )

    }

}