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
package com.gw2tb.manager.ui

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.ui.screens.confirm.ConfirmComponent
import com.gw2tb.manager.ui.screens.details.AddOnDetailsComponent
import com.gw2tb.manager.ui.screens.explore.ExploreComponent
import com.gw2tb.manager.ui.screens.manage.ManageComponent
import kotlinx.serialization.Serializable

interface MasterDetailComponent {

    val page: Value<ChildStack<*, Child>>

    fun navigateToConfirm(plan: ActionPlan)

    fun navigateToExploreAddOns()

    fun navigateToInstalledAddOns()

    fun navigateToSettings()

    @Serializable
    sealed class Config {

        @Serializable
        @ConsistentCopyVisibility
        data class AddOnDetails private constructor(
            val addOnId: AddOnId? = null,
            val ref: LocalAddOnReference? = null
        ) : Config() {

            constructor(addOnId: AddOnId) : this(addOnId = addOnId, ref = null)
            constructor(ref: LocalAddOnReference) : this(addOnId = null, ref = ref)

        }

        @Serializable
        data class Confirm(
            val plan: ActionPlan
        ) : Config()

        @Serializable
        data object ExploreAddOns : Config()

        @Serializable
        data object ManageAddOns : Config()

    }

    sealed class Child {
        data class AddOnDetails(val component: AddOnDetailsComponent) : Child()
        data class Confirm(val component: ConfirmComponent) : Child()
        data class InstalledAddOns(val component: ManageComponent) : Child()
        data class ExploreAddOns(val component: ExploreComponent) : Child()
    }

    sealed class Output {
        data object NavigateToSettings : Output()
    }

}
