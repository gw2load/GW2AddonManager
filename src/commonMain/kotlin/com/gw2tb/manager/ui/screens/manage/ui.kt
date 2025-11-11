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
package com.gw2tb.manager.ui.screens.manage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.no_addons_installed
import com.gw2tb.manager.model.inspections.InspectionAddOnUpdateAvailable
import com.gw2tb.manager.model.inspections.InspectionDuplicateInstallations
import com.gw2tb.manager.model.inspections.InspectionMissingAddOnDependencies
import com.gw2tb.manager.ui.composables.AddOnList
import com.gw2tb.manager.ui.composables.AddOnListItem
import com.gw2tb.manager.ui.composables.AddOnListItemState
import com.gw2tb.manager.ui.theme.ManagerColors
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import kotlin.collections.contains

@Composable
fun ManageAddOns(
    component: ManageComponent,
    modifier: Modifier = Modifier
) {
    val installedAddOns by component.installedAddOns
        .map { installedAddOns -> installedAddOns.distinctBy { it.localAddOn.name to it.localAddOn.kind } }
        .collectAsState(initial = emptyList())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(Color.White, ManagerColors.BackgroundTint)))
    ) {
        AnimatedContent(
            targetState = installedAddOns.isNotEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            }
        ) { hasInstalledAddOns ->
            if (hasInstalledAddOns) {
                val inspections by component.inspections.collectAsState()
                val jobs by component.jobs.collectAsState()

                AddOnList(
                    items = installedAddOns,
                    onClick = { installedAddOn -> component.navigateToDetails(installedAddOn.localAddOn.ref) },
                    modifier = modifier
                        .fillMaxSize()
                        .background(brush = Brush.verticalGradient(listOf(Color.White, ManagerColors.BackgroundTint))),
                    itemModifier = { installedAddOn ->
                        val errorInspection = inspections.find { inspection -> installedAddOn.localAddOn.ref in inspection.affectedRefs && (inspection is InspectionDuplicateInstallations || inspection is InspectionMissingAddOnDependencies) }
                        val availableAddOnUpdate = (inspections.find { inspection -> installedAddOn.localAddOn.ref in inspection.affectedRefs && inspection is InspectionAddOnUpdateAvailable } as? InspectionAddOnUpdateAvailable)?.update

                        Modifier
                            .let {
                                val backgroundTintColor = when {
                                    errorInspection != null -> ManagerColors.NegativeHint
                                    availableAddOnUpdate != null -> ManagerColors.PositiveHint
                                    else -> null
                                }

                                if (backgroundTintColor != null) {
                                    it.background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color.Transparent, backgroundTintColor),
                                            startX = 650F
                                        )
                                    )
                                } else
                                    it
                            }
                    },
                    itemContentPadding = PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 18.dp)
                ) { (localAddOn, listing) ->
                    val inspections = inspections.filter { inspection -> localAddOn.ref in inspection.affectedRefs }

                    AddOnListItem(
                        title = listing?.addOnName ?: localAddOn.name,
                        summary = listing?.addOnSummary ?: "",
                        version = localAddOn.version.toString(),
                        addOnState = if (localAddOn.isEnabled) AddOnListItemState.ENABLED else AddOnListItemState.DISABLED,
                        repairAddOn = component::repairAddOn,
                        updateAddOn = component::updateAddOn,
                        installAddOn = { error("Should never be reached") }, // In this screen, add-ons are already installed
                        setAddOnEnabled = { enabled -> component.setEnabled(localAddOn.ref, enabled) },
                        getJobs = { jobs.filter { localAddOn.ref in it.localAddOns } },
                        inspections = inspections
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.no_addons_installed),
                        color = lerp(ManagerColors.Primary, Color.Black, 0.4F),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
