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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_disable
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_enable
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_install
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_update
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.services.Job
import com.gw2tb.manager.ui.theme.ManagerColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddOnListItem(
    title: String,
    summary: String,
    version: String,
    addOnState: AddOnListItemState,
    updateAddOn: () -> Unit,
    installAddOn: () -> Unit,
    setAddOnEnabled: (enabled: Boolean) -> Unit,
    getJobs: () -> List<Job>,
    modifier: Modifier = Modifier,
    availableAddOnUpdate: AvailableAddOnUpdate? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(4F)
                .alignBy(FirstBaseline),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (addOnState) {
                        AddOnListItemState.NOT_INSTALLED -> Icons.Default.Download
                        AddOnListItemState.ENABLED -> Icons.Default.Check
                        AddOnListItemState.DISABLED -> Icons.Default.Block
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp),
                    tint = if (addOnState != AddOnListItemState.DISABLED) Color.Unspecified else Color.Black.copy(ContentAlpha.medium),
                )

                Text(
                    text = title,
                    modifier = Modifier,
                    color = if (addOnState != AddOnListItemState.DISABLED) Color.Unspecified else Color.Black.copy(ContentAlpha.medium),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Text(
                text = summary,
                color = lerp(ManagerColors.Primary, Color.Black, 0.4F),
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

        Column(
            modifier = Modifier
                .weight(1F)
                .alignBy(FirstBaseline),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = availableAddOnUpdate != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp),
                        tint = ManagerColors.PositiveHighlight
                    )
                }

                Text(
                    text = version,
                    color = Color.Black.copy(alpha = ContentAlpha.medium),
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }

            val jobs = getJobs() // TODO Fix state hoisting for jobs
            when {
                jobs.isNotEmpty() -> {
                    CircularProgressIndicator()
                }
                availableAddOnUpdate != null -> {
                    OutlinedButton(
                        onClick = updateAddOn
                    ) {
                        Text(stringResource(Res.string.addon_update))
                    }
                }
                addOnState == AddOnListItemState.NOT_INSTALLED -> {
                    OutlinedButton(
                        onClick = installAddOn
                    ) {
                        Text(stringResource(Res.string.addon_install))
                    }
                }
                addOnState == AddOnListItemState.DISABLED -> {
                    OutlinedButton(
                        onClick = { setAddOnEnabled(true) }
                    ) {
                        Text(stringResource(Res.string.addon_enable))
                    }
                }
                addOnState == AddOnListItemState.ENABLED -> {
                    OutlinedButton(
                        onClick = { setAddOnEnabled(false) }
                    ) {
                        Text(stringResource(Res.string.addon_disable))
                    }
                }
            }
        }
    }
}

enum class AddOnListItemState { NOT_INSTALLED, ENABLED, DISABLED }
