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
package com.gw2tb.manager.ui.screens.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.actions.Action
import com.gw2tb.manager.actions.ActionDisableAddOn
import com.gw2tb.manager.actions.ActionEnableAddOn
import com.gw2tb.manager.actions.ActionInstallAddOn
import com.gw2tb.manager.actions.ActionRenameAddOn
import com.gw2tb.manager.actions.ActionUninstallAddOn
import com.gw2tb.manager.actions.ActionUpdateAddOn
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.confirm_action_description_disable
import com.gw2tb.manager.gw2addonmanager.generated.resources.confirm_action_description_enable
import com.gw2tb.manager.gw2addonmanager.generated.resources.confirm_action_description_install
import com.gw2tb.manager.gw2addonmanager.generated.resources.confirm_action_description_rename
import com.gw2tb.manager.gw2addonmanager.generated.resources.confirm_action_description_uninstall
import com.gw2tb.manager.gw2addonmanager.generated.resources.confirm_action_description_update
import com.gw2tb.manager.gw2addonmanager.generated.resources.confirm_heading
import com.gw2tb.manager.gw2addonmanager.generated.resources.confirm_no
import com.gw2tb.manager.gw2addonmanager.generated.resources.confirm_yes
import com.gw2tb.manager.model.InstalledAddOn
import com.gw2tb.manager.model.LocalAddOnReference
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.toDisplayString
import com.gw2tb.manager.ui.composables.Checkbox
import com.gw2tb.manager.ui.composables.OutlinedButton
import com.gw2tb.manager.ui.theme.ManagerColors
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Path

@Composable
fun ConfirmActionPlan(component: ConfirmComponent) {
    val plan = component.plan
    val orderedActions = plan.actions.toList() + plan.effects.toList()
    val orderedOptionalActions = plan.optionalActions.toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(listOf(Color.White, ManagerColors.BackgroundTint)))
            .padding(PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 18.dp))
    ) {
        Text(
            text = stringResource(Res.string.confirm_heading),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .height((1f / LocalDensity.current.density).dp)
                .background(
                    brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, ManagerColors.Primary, Color.Transparent))
                )
        ) {}

        Spacer(Modifier.height(4.dp))

        var includeOptional by remember { mutableStateOf(true) }

        LazyColumn(
            modifier = Modifier
                .weight(1F, fill = true)
        ) {
            items(orderedActions.size) { index ->
                val action = orderedActions[index]

                ActionItem(
                    action = action,
                    getAddOnListing = component::getAddOnListing,
                    getInstalledAddOn = component::getInstalledAddOn,
                    getSelectedGameDirectory = component::selectedGameDirectory
                )
            }

            if (orderedOptionalActions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("•")

                        Checkbox(
                            checked = includeOptional,
                            onClick = {
                                includeOptional = !includeOptional
                            },
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "Clean up game directory (Optional)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .height((1f / LocalDensity.current.density).dp)
                                .background(
                                    brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, ManagerColors.Primary, Color.Transparent))
                                )
                        ) {}
                    }
                }

                items(orderedOptionalActions.size) { index ->
                    val action = orderedOptionalActions[index]
                    val contentColor = if (includeOptional) LocalContentColor.current else LocalContentColor.current.copy(alpha = ContentAlpha.disabled)

                    CompositionLocalProvider(
                        LocalContentColor provides contentColor,
                        LocalTextStyle provides LocalTextStyle.current.copy(color = contentColor)
                    ) {
                        ActionItem(
                            action = action,
                            getAddOnListing = component::getAddOnListing,
                            getInstalledAddOn = component::getInstalledAddOn,
                            getSelectedGameDirectory = component::selectedGameDirectory,
                            modifier = Modifier
                                .padding(start = 16.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .height((1f / LocalDensity.current.density).dp)
                .background(
                    brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, ManagerColors.Primary, Color.Transparent))
                )
        ) {}

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Row(
                modifier = Modifier.width(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1F),
                    baseColor = ManagerColors.PositiveHighlight,
                    onClick = { component.confirm(includeOptional = includeOptional) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.confirm_yes),
                        fontSize = 16.sp
                    )
                }

                OutlinedButton(
                    modifier = Modifier
                        .weight(1F),
                    baseColor = ManagerColors.NegativeHighlight,
                    onClick = component::cancel,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null
                        )
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.confirm_no),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionItem(
    action: Action,
    getAddOnListing: (id: AddOnId) -> StateFlow<AddOnListing?>,
    getInstalledAddOn: (ref: LocalAddOnReference) -> StateFlow<InstalledAddOn?>,
    getSelectedGameDirectory: () -> StateFlow<Path?>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("•")

        Icon(
            imageVector = when (action) {
                is ActionDisableAddOn -> Icons.Default.Block
                is ActionEnableAddOn -> Icons.Default.Check
                is ActionInstallAddOn -> Icons.Default.Download
                is ActionRenameAddOn -> Icons.Default.Edit
                is ActionUninstallAddOn -> Icons.Default.Delete
                is ActionUpdateAddOn -> Icons.Default.Update
            },
            contentDescription = null,
            modifier = Modifier
                .size(16.dp),
            tint = LocalContentColor.current
        )

        Text(
            text = stringDescription(
                action = action,
                getAddOnListing = getAddOnListing,
                getInstalledAddOn = getInstalledAddOn,
                getSelectedGameDirectory = getSelectedGameDirectory
            ),
            modifier = Modifier,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun stringDescription(
    action: Action,
    getAddOnListing: (id: AddOnId) -> StateFlow<AddOnListing?>,
    getInstalledAddOn: (ref: LocalAddOnReference) -> StateFlow<InstalledAddOn?>,
    getSelectedGameDirectory: () -> StateFlow<Path?>
): String {
    val selectedGameDirectory by getSelectedGameDirectory().collectAsState()

    val resource = when (action) {
        is ActionDisableAddOn -> Res.string.confirm_action_description_disable
        is ActionEnableAddOn -> Res.string.confirm_action_description_enable
        is ActionInstallAddOn -> Res.string.confirm_action_description_install
        is ActionRenameAddOn -> Res.string.confirm_action_description_rename
        is ActionUninstallAddOn -> Res.string.confirm_action_description_uninstall
        is ActionUpdateAddOn -> Res.string.confirm_action_description_update
    }

    val args = when (action) {
        is ActionDisableAddOn, is ActionEnableAddOn, is ActionUninstallAddOn -> {
            val installedAddOn by getInstalledAddOn(action.affectedLocalAddOn!!).collectAsState()
            arrayOf(installedAddOn?.toDisplayString(gameDirectory = selectedGameDirectory) ?: "<unknown>")
        }
        is ActionInstallAddOn -> {
            val listing by getAddOnListing(action.affectedAddOnId).collectAsState()
            arrayOf(listing?.addOnName ?: "<unknown>")
        }
        is ActionRenameAddOn -> {
            val installedAddOn by getInstalledAddOn(action.ref).collectAsState()
            arrayOf(
                installedAddOn?.listing?.addOnName ?: installedAddOn?.localAddOn?.name ?: "<unknown>",
                action.newFileName,
                installedAddOn?.localAddOn?.path?.fileName.toString(),
            )
        }
        is ActionUpdateAddOn -> {
            val installedAddOn by getInstalledAddOn(action.ref).collectAsState()
            arrayOf(
                installedAddOn?.listing?.addOnName ?: installedAddOn?.localAddOn?.name ?: "<unknown>",
                installedAddOn?.listing?.download?.version ?: "<unknown>",
                installedAddOn?.localAddOn?.version?.toString() ?: "<unknown>"
            )
        }
    }

    return stringResource(resource, *args)
}
