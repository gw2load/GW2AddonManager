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
package com.gw2tb.manager.ui.screens.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_description_unknown
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_vendor_unknown
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.inspections.Inspection
import com.gw2tb.manager.inspections.InspectionAddOnUpdateAvailable
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.ui.composables.OutlinedButton
import com.gw2tb.manager.ui.composables.TextButton
import com.gw2tb.manager.ui.theme.ManagerColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Composable
fun AddOnDetails(component: AddOnDetailsComponent) {
    val listing by component.listing.collectAsState()
    val localAddOns by component.localAddOns.collectAsState()
    val inspections by component.inspections.collectAsState()

    if (listing == null && localAddOns.isEmpty()) {
        component.navigateBack()
        return
    }

    AddOnDetails(
        component = component,
        addOnListing = listing,
        localAddOns = localAddOns,
        installAddOn = {
            component.installAddOn(listing?.id ?: error("Unknown listing"))
        },
        uninstallAddOn = {
            val localAddOn = localAddOns.singleOrNull() ?: error("Cannot uninstall ambiguous add-on")
            component.uninstallAddOn(localAddOn.ref)
        },
        enableAddOn = {
            val localAddOn = localAddOns.singleOrNull() ?: error("Cannot enable ambiguous add-on")
            component.enableAddOn(localAddOn.ref)
        },
        disableAddOn = {
            val localAddOn = localAddOns.singleOrNull() ?: error("Cannot disable ambiguous add-on")
            component.disableAddOn(localAddOn.ref)
        },
        updateAddOn = component::updateAddOn,
        navigateToVendor = component::navigateToVendor,
        inspections = inspections
    )
}

@Composable
private fun AddOnDetails(
    component: AddOnDetailsComponent,
    addOnListing: AddOnListing?,
    localAddOns: List<LocalAddOn>,
    installAddOn: () -> Unit,
    uninstallAddOn: () -> Unit,
    enableAddOn: () -> Unit,
    disableAddOn: () -> Unit,
    updateAddOn: (AvailableAddOnUpdate) -> Unit,
    navigateToVendor: (String) -> Unit,
    inspections: Iterable<Inspection>
) {
    val localDisplayAddOn = localAddOns.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(Color.White, ManagerColors.BackgroundTint)))
            .padding(PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1F, fill = true)
            ) {
                Text(
                    text = addOnListing?.addOnName ?: localDisplayAddOn?.name ?: error("No add-on listing or local add-on provided"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1F, fill = true)
                            .alignBy(FirstBaseline),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        CompositionLocalProvider(
                            LocalTextStyle provides LocalTextStyle.current.copy(
                                color = Color.Black.copy(ContentAlpha.medium),
                                fontSize = 12.sp
                            )
                        ) {
                            Text(
                                text = addOnListing?.download?.version?.toString() ?: localDisplayAddOn?.version?.toString() ?: "Version unavailable",
                                modifier = Modifier
                                    .alignBy(FirstBaseline),
                                maxLines = 1,
                                softWrap = false
                            )

                            Text("•")

                            Row(
                                modifier = Modifier
                                    .alignBy(FirstBaseline)
                            ) {
                                if (addOnListing != null) {
                                    Text(
                                        text = "by ",
                                        modifier = Modifier
                                            .alignBy(FirstBaseline)
                                    )

                                    if (addOnListing.vendorUrl != null) {
                                        TextButton(
                                            text = addOnListing.vendorName,
                                            onClick = { navigateToVendor(addOnListing.vendorUrl) },
                                            modifier = Modifier
                                                .alignBy(FirstBaseline)
                                        )
                                    } else {
                                        Text(
                                            text = addOnListing.vendorName,
                                            modifier = Modifier
                                                .alignBy(FirstBaseline)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = stringResource(Res.string.addon_vendor_unknown),
                                        modifier = Modifier
                                            .alignBy(FirstBaseline)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .animateContentSize()
            ) {
                val availableUpdate = (inspections.singleOrNull() as? InspectionAddOnUpdateAvailable)?.update
                val singleLocalAddOn = localAddOns.singleOrNull()

                AnimatedVisibility(visible = availableUpdate != null) {
                    OutlinedButton(
                        onClick = { updateAddOn(availableUpdate!!) },
                        baseColor = ManagerColors.PositiveHighlight
                    ) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = ManagerColors.PositiveHighlight
                        )
                    }
                }

                AnimatedVisibility(visible = singleLocalAddOn != null) {
                    val isEnabled = singleLocalAddOn!!.isEnabled

                    OutlinedButton(
                        onClick = { if (isEnabled) disableAddOn() else enableAddOn() }
                    ) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                if (localAddOns.isNotEmpty()) {
                    OutlinedButton(
                        onClick = uninstallAddOn,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = installAddOn,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .height((1f / LocalDensity.current.density).dp)
                .background(
                    brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, ManagerColors.Primary, Color.Transparent))
                )
        ) {}

        Spacer(modifier = Modifier.height(4.dp))

        Box {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                if (localAddOns.size > 1) {
                    Spacer(modifier = Modifier.height(4.dp))

                    DuplicateInstallationsBox(
                        localAddOns = localAddOns,
                        deleteAddOns = component::deleteAddOns,
                        modifier = Modifier
                            .fillMaxWidth(),
                        pathToDisplayString = { path ->
                            component.selectedGameDirectory
                                .map { gameDirectory -> gameDirectory?.relativize(path)?.toString() ?: path.absolutePathString() }
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(text = addOnListing?.addOnDescription ?: stringResource(Res.string.addon_description_unknown))

                Spacer(
                    modifier = Modifier
                        .heightIn(min = 8.dp)
                        .weight(1F, fill = true)
                )

                // TODO Add links to sources, etc.
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                style = LocalScrollbarStyle.current.copy(
                    thickness = 6.dp,
                    shape = RectangleShape
                )
            )
        }
    }
}

@Composable
private fun DuplicateInstallationsBox(
    localAddOns: List<LocalAddOn>,
    deleteAddOns: (Iterable<LocalAddOn>) -> Unit,
    modifier: Modifier = Modifier,
    pathToDisplayString: (Path) -> Flow<String> = { flowOf(it.absolutePathString()) }
) {
    val baseColor = ManagerColors.NegativeHighlight
    val darkColor = lerp(baseColor, Color.Black, 0.4F)

    Column(
        modifier = modifier
            .background(brush = Brush.verticalGradient(listOf(Color.White, ManagerColors.NegativeHint)))
            .border(width = 1.dp, color = ManagerColors.NegativeHint, shape = RectangleShape)
            .padding(all = 8.dp)
    ) {
        CompositionLocalProvider(
            LocalContentColor provides darkColor,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                )

                Text(
                    text = "The add-on was found in more than one location. Select one installation to keep:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Column {
            localAddOns.forEachIndexed { index, localAddOn ->
                val pathDisplayString by pathToDisplayString(localAddOn.path).collectAsState(initial = localAddOn.path.absolutePathString())

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "•",
                        color = lerp(ManagerColors.NegativeHighlight, Color.Black, 0.4F)
                    )

                    TextButton(
                        text = {
                            Text(
                                text = pathDisplayString,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        onClick = { deleteAddOns(localAddOns - localAddOn) },
                        color = ManagerColors.NegativeHighlight
                    )

                    if (index == 0) {
                        Text(
                            text = "(Recommended)",
                            color = ManagerColors.NegativeHighlight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
