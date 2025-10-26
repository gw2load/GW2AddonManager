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
package com.gw2tb.manager.manager_manifest

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator

internal val json = Json {
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

fun parseAddOnManagerManifest(source: String): AddOnManagerManifest =
    json.decodeFromString<AddOnManagerManifest>(source)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("format")
sealed interface AddOnManagerManifest {

    @Serializable
    @SerialName("1")
    data class V1(
        val versions: List<Version>
    ) : AddOnManagerManifest {

        @Serializable
        data class Version(
            val version: List<UShort>,
            val versionString: String,
            val artifacts: List<Artifacts>
        ) {

            @Serializable
            data class Artifacts(
                val type: String,
                val downloadUrl: String,
                val downloadSha512: String
            )

        }

    }

}
