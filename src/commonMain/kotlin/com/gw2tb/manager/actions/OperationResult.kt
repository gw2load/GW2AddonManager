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

/** The result of executing an operation described by an [action plan][ActionPlan]. */
@Serializable
sealed interface OperationResult {

    /**
     * Indicates that the operation could not be executed because it would require unapproved side effects to function
     * correctly.
     *
     * Typically, the [suggested action plan][plan] should be presented to the user for confirmation before proceeding
     * with the operation.
     *
     * @property plan   the suggested action plan to execute the operation
     */
    @Serializable
    data class RequiresConfirmation(val plan: ActionPlan) : OperationResult

    /** Indicates that the operation completed successfully. */
    @Serializable
    data object Success : OperationResult

}
