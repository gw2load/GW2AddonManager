/*
 * GW2AddOnManager
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
package com.gw2tb.manager.model.local

import androidx.compose.runtime.Immutable
import com.gw2tb.manager.platform.win32.getAddOnInfo
import java.nio.file.Path

fun LocalAddOn(path: Path, kind: LocalAddOn.Kind): LocalAddOn? {
    val addOnInfo = path.getAddOnInfo() ?: return null

    return LocalAddOn(
        kind = kind,
        path = path,
        name = addOnInfo.name,
        version = addOnInfo.version
    )
}

/**
 * Represents a locally installed add-on.
 *
 * @param kind      the kind of the add-on
 * @param name      the name of the add-on
 * @param path      the path to the add-on binary
 * @param isEnabled whether the add-on is enabled
 */
@Immutable
data class LocalAddOn(
    val kind: Kind,
    val name: String,
    val path: Path,
    val version: AddOnVersion,
    val isEnabled: Boolean = !path.fileName.toString().endsWith(".disabled")
) {

    /** A kind of add-on. */
    enum class Kind {
        /** An _addon-loader_ (legacy) add-on. */
        ADDON_LOADER,
        /** GW2Load itself. */
        GW2_LOAD_LOADER,
        /** A _GW2Load_ add-on. */
        GW2_LOAD_ADDON
    }

}