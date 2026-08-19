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
package com.gw2tb.manager.ui.screens.explore

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
import androidx.compose.ui.unit.dp
import com.gw2tb.manager.model.catalog.isMatching
import com.gw2tb.manager.inspections.InspectionAddOnUpdateAvailable
import com.gw2tb.manager.inspections.InspectionDuplicateInstallations
import com.gw2tb.manager.inspections.InspectionMissingAddOnDependencies
import com.gw2tb.manager.ui.composables.AddOnList
import com.gw2tb.manager.ui.composables.AddOnListItem
import com.gw2tb.manager.ui.composables.AddOnListItemState
import com.gw2tb.manager.ui.theme.ManagerColors

@Composable
fun ExploreAddOns(
    component: ExploreComponent,
    modifier: Modifier = Modifier
) {
    val addOnListings by component.addOnListings.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(Color.White, ManagerColors.BackgroundTint)))
    ) {
        AnimatedContent(
            targetState = addOnListings.isNotEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            }
        ) { hasAddOnListings ->
            if (hasAddOnListings) {
                val localAddOns by component.localAddOns.collectAsState()
                val inspections by component.inspections.collectAsState()

                val jobs by component.jobs.collectAsState()

                AddOnList(
                    items = addOnListings,
                    onClick = { item -> component.navigateToAddOnDetails(item.id) },
                    modifier = Modifier
                        .fillMaxSize(),
                    itemKey = { _, addOnListing -> addOnListing.id },
                    itemModifier = { addOnListing ->
                        val localAddOn = localAddOns.find { addOnListing isMatching it }
                        val errorInspection = inspections.find { inspection -> localAddOn?.ref in inspection.affectedRefs && (inspection is InspectionDuplicateInstallations || inspection is InspectionMissingAddOnDependencies) }
                        val availableAddOnUpdate = (inspections.find { inspection -> localAddOn?.ref in inspection.affectedRefs && inspection is InspectionAddOnUpdateAvailable } as? InspectionAddOnUpdateAvailable)?.update

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
                ) { item ->
                    val localAddOn = localAddOns.find { item isMatching it }
                    val inspections = inspections.filter { inspection -> localAddOn?.ref in inspection.affectedRefs }

                    AddOnListItem(
                        title = item.addOnName,
                        summary = item.addOnSummary,
                        version = item.download!!.version.toString(),
                        addOnState = when {
                            localAddOn == null -> AddOnListItemState.NOT_INSTALLED
                            localAddOn.isEnabled -> AddOnListItemState.ENABLED
                            else -> AddOnListItemState.DISABLED
                        },
                        repairAddOn = component::repairAddOn,
                        updateAddOn = component::updateAddOn,
                        installAddOn = { component.installAddOn(item.id) },
                        setAddOnEnabled = { enabled -> component.setEnabled(localAddOn!!.ref, enabled) },
                        getJobs = { jobs.filter { item.id in it.addOnListings } },
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
                        text = "No Add On List"
                    )
                }
            }
        }
    }
}
