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
package com.gw2tb.manager.ui.screens.exception

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.exceptions.AddOnManifestException
import com.gw2tb.manager.exceptions.ManagerManifestException
import com.gw2tb.manager.exceptions.UnexpectedException
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.copy_report
import com.gw2tb.manager.gw2addonmanager.generated.resources.exception_addon_manifest_description
import com.gw2tb.manager.gw2addonmanager.generated.resources.exception_addon_manifest_title
import com.gw2tb.manager.gw2addonmanager.generated.resources.exception_manager_manifest_description
import com.gw2tb.manager.gw2addonmanager.generated.resources.exception_manager_manifest_title
import com.gw2tb.manager.gw2addonmanager.generated.resources.exception_unexpected_description
import com.gw2tb.manager.gw2addonmanager.generated.resources.exception_unexpected_title
import com.gw2tb.manager.gw2addonmanager.generated.resources.icon_github
import com.gw2tb.manager.gw2addonmanager.generated.resources.open_logs_directory
import com.gw2tb.manager.gw2addonmanager.generated.resources.report_issue
import com.gw2tb.manager.internal.BuildConfig
import com.gw2tb.manager.model.notifications.Urgency
import com.gw2tb.manager.ui.composables.LocalApplicationInfo
import com.gw2tb.manager.ui.composables.TextButton
import com.gw2tb.manager.ui.theme.ManagerColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun Exception(
    component: ExceptionComponent
) {
    Box {
        val scrollState = rememberScrollState()

        val backgroundHintColor = when (component.exception.urgency) {
            Urgency.Informational -> ManagerColors.BackgroundTint
            Urgency.Warning -> ManagerColors.WarnHint
            Urgency.Error, Urgency.Critical -> ManagerColors.NegativeHint
        }

        val onBackgroundColor = when (component.exception.urgency) {
            Urgency.Informational -> ManagerColors.Primary
            Urgency.Warning -> ManagerColors.WarnHighlight
            Urgency.Error, Urgency.Critical -> ManagerColors.NegativeHighlight
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(listOf(Color.White, backgroundHintColor)))
                .padding(PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 18.dp))
                .verticalScroll(scrollState)
        ) {
            Text(
                text = when (component.exception) {
                    is AddOnManifestException -> stringResource(Res.string.exception_addon_manifest_title)
                    is ManagerManifestException -> stringResource(Res.string.exception_manager_manifest_title)
                    is UnexpectedException -> stringResource(Res.string.exception_unexpected_title)
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (component.exception) {
                    is AddOnManifestException -> stringResource(Res.string.exception_addon_manifest_description)
                    is ManagerManifestException -> stringResource(Res.string.exception_manager_manifest_description)
                    is UnexpectedException -> stringResource(Res.string.exception_unexpected_description)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TextButton(
                    text = stringResource(Res.string.report_issue),
                    onClick = { component.openLink(BuildConfig.ISSUES_URL) },
                    color = ManagerColors.WarnHighlight,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.icon_github),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                val logsDir = LocalApplicationInfo.current.logsDir

                TextButton(
                    text = stringResource(Res.string.open_logs_directory),
                    onClick = { component.openDirectory(logsDir) },
                    color = ManagerColors.WarnHighlight,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                val clipboard = LocalClipboard.current

                TextButton(
                    text = stringResource(Res.string.copy_report),
                    onClick = { component.copy(clipboard) },
                    color = ManagerColors.WarnHighlight,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CopyAll,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height((1f / LocalDensity.current.density).dp)
                    .background(
                        brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, onBackgroundColor, Color.Transparent))
                    )
            ) {}

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = component.report,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 2.dp),
            style = LocalScrollbarStyle.current.copy(
                thickness = 6.dp,
                shape = RectangleShape
            )
        )
    }
}
