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
package com.gw2tb.manager.ui.screens.confirm

import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.InstalledAddOn
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path

interface ConfirmComponent {

    val plan: ActionPlan

    val selectedGameDirectory: StateFlow<Path?>

    fun getAddOnListing(id: AddOnId): StateFlow<AddOnListing?>

    fun getInstalledAddOn(ref: LocalAddOnReference): StateFlow<InstalledAddOn?>

    fun cancel()

    fun confirm(includeOptional: Boolean)

    sealed class Output {
        data object Exit : Output()
    }

}
