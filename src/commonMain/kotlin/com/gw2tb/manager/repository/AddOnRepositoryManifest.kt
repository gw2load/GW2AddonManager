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

import com.gw2tb.manager.model.AddOnId
import kotlinx.serialization.*
import kotlinx.serialization.json.*

internal val json = Json {
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

internal fun parseAddOnManifest(str: String): List<AddOnManifestV1.AddOnEntry> {
    return json.decodeFromString<AddOnManifestContainer>(str)
        .let { container ->
            when (container.version) {
                1 -> json.decodeFromJsonElement<AddOnManifestV1>(container.data)
                else -> throw IllegalArgumentException("Unsupported manifest version: ${container.version}")
            }
        }
        .addons
        .toList()
}

@Serializable
data class AddOnManifestContainer(
    val version: Int,
    val data: JsonObject
)

sealed interface AddOnManifest

@Serializable
data class AddOnManifestV1(
    val addons: List<AddOnEntry>,
    val loader: Loader
) : AddOnManifest {

    @Serializable
    data class AddOnEntry(
        val `package`: Package,
        val host: JsonObject, // We don't really care about this atm
        val installation: Installation,
        val release: Release? = null,
        val prerelease: Release? = null,
        @SerialName("addon_names")
        val addonNames: List<String>? = null
    ) {

        @Serializable
        data class Package(
            val id: AddOnId,
            val name: String,
            val description: String,
            val tooltip: String,
            val website: String,
            val developer: String,
            @SerialName("issue_tracker")
            val issueTracker: String,
            val vcs: String? = null,
            val dependencies: List<AddOnId>? = null,
            @SerialName("optional_dependencies")
            val optionalDependencies: List<AddOnId>? = null,
            val conflicts: List<AddOnId>? = null
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

    @Serializable
    data class Loader(
        val release: Release? = null,
        val prerelease: Release? = null,
    )
}
