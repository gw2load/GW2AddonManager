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
package com.gw2tb.manager.exceptions

import com.gw2tb.manager.model.notifications.Urgency
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
sealed class AddOnManifestException : ManagerException() {

    override val urgency: Urgency get() = Urgency.Warning

    @ConsistentCopyVisibility
    @Serializable
    data class FetchException private constructor(
        val httpStatusCode: Int? = null,
        override val cause: String
    ) : AddOnManifestException() {

        constructor(status: HttpStatusCode, cause: Throwable) : this(httpStatusCode = status.value, cause = cause.stackTraceToString())

        constructor(cause: Throwable) : this(cause = cause.stackTraceToString())

    }

    @Serializable
    data class ParseException(
        override val cause: String
    ) : AddOnManifestException() {

        constructor(cause: Throwable) : this(cause.stackTraceToString())

    }

}
