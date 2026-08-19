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
package com.gw2tb.manager.actions

import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.LocalAddOnReference
import kotlinx.serialization.Serializable

/**
 * Disables a [local add-on][ref].
 *
 * @param ref   the reference to the local add-on to disable
 */
@Serializable
data class ActionDisableAddOn(val ref: LocalAddOnReference) : Action {
    override val affectedAddOnId: AddOnId? get() = null
    override val affectedLocalAddOn: LocalAddOnReference get() = ref
}

/**
 * Enables a [local add-on][ref].
 *
 * @param ref   the reference to the local add-on to enable
 */
@Serializable
data class ActionEnableAddOn(val ref: LocalAddOnReference) : Action {
    override val affectedAddOnId: AddOnId? get() = null
    override val affectedLocalAddOn: LocalAddOnReference get() = ref
}

/**
 * Installs an add-on.
 *
 * @param id    the ID of the add-on to install
 */
@Serializable
data class ActionInstallAddOn(val id: AddOnId) : Action {
    override val affectedAddOnId: AddOnId get() = id
    override val affectedLocalAddOn: LocalAddOnReference? get() = null
}

/**
 * Renames (or moves) a local add-on.
 *
 * @param ref           the reference to the local add-on to rename
 * @param newFileName   the new file name for the add-on
 */
@Serializable
data class ActionRenameAddOn(
    val ref: LocalAddOnReference,
    val newFileName: String
) : Action {
    override val affectedAddOnId: AddOnId? get() = null
    override val affectedLocalAddOn: LocalAddOnReference get() = ref
}

/**
 * Uninstalls a [local add-on][ref].
 *
 * @param ref   the reference to the local add-on to uninstall
 */
@Serializable
data class ActionUninstallAddOn(val ref: LocalAddOnReference) : Action {
    override val affectedAddOnId: AddOnId? get() = null
    override val affectedLocalAddOn: LocalAddOnReference get() = ref
}

/**
 * Updates an add-on.
 *
 * @param id    the ID of the add-on to update
 */
@Serializable
data class ActionUpdateAddOn(val ref: LocalAddOnReference, val id: AddOnId) : Action {
    override val affectedAddOnId: AddOnId get() = id
    override val affectedLocalAddOn: LocalAddOnReference get() = ref
}
