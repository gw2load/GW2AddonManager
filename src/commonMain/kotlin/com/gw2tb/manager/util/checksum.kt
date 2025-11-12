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
package com.gw2tb.manager.util

import java.io.FileInputStream
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.isRegularFile

fun Path.contentChecksum(
    digest: MessageDigest = MessageDigest.getInstance("SHA-512")
): String {
    require(isRegularFile()) { "File is not a regular file: '$this'" }
    digest.reset()

    FileInputStream(toFile()).use { input ->
        val buffer = ByteArray(8192)
        var bytesRead: Int

        while (input.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }

    val hashBytes = digest.digest()
    return hashBytes.joinToString("") { "%02x".format(it) }
}
