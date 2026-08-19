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
package com.gw2tb.manager.ui.theme.indication

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.runtime.Immutable

@Immutable
class IndicationAlpha(
    val draggedAlpha: Float,
    val focusedAlpha: Float,
    val hoveredAlpha: Float,
    val pressedAlpha: Float
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RippleAlpha) return false

        if (draggedAlpha != other.draggedAlpha) return false
        if (focusedAlpha != other.focusedAlpha) return false
        if (hoveredAlpha != other.hoveredAlpha) return false
        if (pressedAlpha != other.pressedAlpha) return false

        return true
    }

    override fun hashCode(): Int {
        var result = draggedAlpha.hashCode()
        result = 31 * result + focusedAlpha.hashCode()
        result = 31 * result + hoveredAlpha.hashCode()
        result = 31 * result + pressedAlpha.hashCode()
        return result
    }

    override fun toString(): String =
        "IndicationAlpha(draggedAlpha=$draggedAlpha, focusedAlpha=$focusedAlpha, hoveredAlpha=$hoveredAlpha, pressedAlpha=$pressedAlpha)"

}
