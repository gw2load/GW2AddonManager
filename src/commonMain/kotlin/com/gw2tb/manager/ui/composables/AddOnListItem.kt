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
package com.gw2tb.manager.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_disable_tooltip
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_enable_tooltip
import com.gw2tb.manager.gw2addonmanager.generated.resources.icon_download
import com.gw2tb.manager.services.Job
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.Cursor

@Composable
fun AddOnListItem(
    title: String,
    addOnState: AddOnListItemState,
    selected: Boolean,
    onClick: () -> Unit,
    installAddOn: () -> Unit,
    setAddOnEnabled: (enabled: Boolean) -> Unit,
    getJobs: () -> List<Job>,
    modifier: Modifier = Modifier,
    showUpdateIndicator: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1F, fill = true)
                .alignBy(FirstBaseline),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .alignBy(FirstBaseline)
                    .weight(1F, fill = false),
                fontWeight = FontWeight.Bold,
                maxLines = 3
            )

            // TODO Rethink this indicator
            AnimatedVisibility(visible = showUpdateIndicator) {
                Text(
                    "NEW VERSION",
                    modifier = Modifier
                        .align(Alignment.Top),
                    color = lerp(Color(0xFF8ad3d3), Color.Black, 0.4F),
                    fontSize = 9.sp
                )
            }
        }

        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        val color = if (isHovered) {
            Color(0xFF8ad3d3)
        } else {
            lerp(Color(0xFF8ad3d3), Color.Black, 0.4F)
        }

        val jobs = getJobs() // TODO Fix state hoisting for jobs

        when {
            jobs.isNotEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
            }
            addOnState == AddOnListItemState.NOT_INSTALLED -> {
                Icon(
                    painter = painterResource(Res.drawable.icon_download),
                    contentDescription = "Install $title", // TODO i18n
                    modifier = Modifier
                        .clip(CircleShape)
                        .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClickLabel = "Install $title", // TODO i18n
                            role = Role.Button,
                            onClick = installAddOn
                        )
                        .padding(4.dp)
                        .size(20.dp),
                    tint = color
                )
            }
            else -> {
                @OptIn(ExperimentalFoundationApi::class)
                TooltipArea(
                    tooltip = {
                        Surface(
                            elevation = 2.dp
                        ) {
                            Text(
                                text = stringResource(if (addOnState == AddOnListItemState.ENABLED) Res.string.addon_disable_tooltip else Res.string.addon_enable_tooltip),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .alignBy(LastBaseline),
                    tooltipPlacement = TooltipPlacement.CursorPoint(offset = DpOffset(0.dp, 20.dp))
                ) {
                    Checkbox(
                        checked = addOnState == AddOnListItemState.ENABLED,
                        onClick = { setAddOnEnabled(addOnState != AddOnListItemState.ENABLED) }
                    )
                }
            }
        }
    }
}

enum class AddOnListItemState { NOT_INSTALLED, ENABLED, DISABLED }