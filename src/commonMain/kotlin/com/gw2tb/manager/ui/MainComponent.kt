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
package com.gw2tb.manager.ui

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.exceptions.ManagerException
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.services.Job
import com.gw2tb.manager.ui.screens.settings.SettingsComponent
import kotlinx.coroutines.flow.StateFlow

interface MainComponent {

    val areAutoUpdatesEnabled: StateFlow<Boolean>

    val canPlay: StateFlow<Boolean>

    val jobs: StateFlow<List<Job>>

    val page: Value<ChildStack<*, Child>>

    fun navigateToAddOnDetails(id: AddOnId)

    fun navigateToAddOnDetails(ref: LocalAddOnReference)

    fun navigateToConfirm(plan: ActionPlan)

    fun navigateToException(exception: ManagerException)

    fun navigateToExploreAddOns()

    fun navigateToHelp()

    fun navigateToInstalledAddOns()

    fun navigateToSettings()

    fun openLink(url: String)

    fun play()

    fun setAutoUpdatesEnabled(enabled: Boolean)

    sealed class Child {

        data class MasterDetail(val component: MasterDetailComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()

    }

}
