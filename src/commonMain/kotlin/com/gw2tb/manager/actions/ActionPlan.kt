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
package com.gw2tb.manager.actions

import kotlinx.serialization.Serializable

/**
 * An action plan is a collection of [actions][Action] that are to be executed.
 *
 * @param actions           the primary user-initiated actions to be executed
 * @param effects           the secondary effects that will be executed as part of the action plan but are typically
 *                          necessities for the primary actions to succeed. For example, if the user wants to install an
 *                          add-on with a dependency on another add-on, the installation of the dependency would be an effect.
 * @param optionalActions   optional actions that may be executed at the user's discretion
 */
@Serializable
data class ActionPlan(
    val stage: Stage,
    val actions: Set<Action>,
    val effects: Set<Action>,
    val optionalActions: Set<Action>
) {

    enum class Stage {
        PROPOSED,
        CONFIRMED
    }

}
