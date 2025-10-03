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
package com.gw2tb.manager.ui

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.gw2tb.manager.model.notifications.Notification
import com.gw2tb.manager.ui.screens.setup.SetupComponent
import kotlinx.coroutines.flow.StateFlow

interface RootComponent {

    val notifications: StateFlow<List<Notification>>

    val page: Value<ChildStack<*, Child>>

    fun navigateToSettings()

    fun onNotificationClick(notification: Notification)

    sealed class Child {

        data class Main(val component: MainComponent) : Child()
        data class Setup(val component: SetupComponent) : Child()

    }

}
