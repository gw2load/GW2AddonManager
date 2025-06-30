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
package com.gw2tb.manager.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.gw2tb.manager.gw2addonmanager.generated.resources.*
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.internal.BuildConfig
import com.gw2tb.manager.ui.composables.*
import com.gw2tb.manager.ui.screens.explore.ExploreAddOnsDetails
import com.gw2tb.manager.ui.screens.explore.ExploreAddOnsMaster
import com.gw2tb.manager.ui.screens.manage.ManageAddOnsDetails
import com.gw2tb.manager.ui.screens.manage.ManageAddOnsMaster
import com.gw2tb.manager.ui.screens.settings.SettingsScreen
import com.gw2tb.manager.ui.screens.setup.SetupScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.Cursor
import java.util.Locale

@Composable
fun WindowScope.AddOnManager(
    component: RootComponent,
    selectLocale: (Locale) -> Unit,
    minimizeWindow: () -> Unit,
    exitApplication: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(Res.drawable.background),
            contentDescription = null, // No point in giving the background image a description
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .padding(start = 46.dp, top = 54.dp, end = 128.dp, bottom = 19.dp)
                .fillMaxSize()
        ) {
            WindowDraggableArea {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .height(21.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LanguageSelector(
                                selectLocale = selectLocale
                            )

                            // TODO Implement notifications
//                            val childStack by component.page.subscribeAsState()
//                            if (childStack.active.instance !is AddOnManagerComponent.Child.Setup) {
//                                NotificationBar(
//                                    notifications = component.notifications,
//                                    onNotificationClick = component::onNotificationClick
//                                )
//                            }
                        }

                        Spacer(Modifier.weight(1F, fill = true))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.icon_minimize),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color = Color(0xFF8ad3d3))
                                    .border(width = Dp.Hairline, color = Color.Black.copy(alpha = 0.2F))
                                    .clickable(
                                        onClickLabel = stringResource(Res.string.window_minimize),
                                        role = Role.Button,
                                        onClick = minimizeWindow
                                    )
                                    .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                            )

                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color = Color(0xFF8ad3d3))
                                    .border(width = Dp.Hairline, color = Color.Black.copy(alpha = 0.2F))
                                    .clickable(
                                        onClickLabel = stringResource(Res.string.window_close),
                                        role = Role.Button,
                                        onClick = exitApplication
                                    )
                                    .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                            )
                        }
                    }

                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = stringResource(Res.string.app_logo_alt),
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                            .width(380.dp)
                    )
                }
            }

            Children(
                stack = component.page,
                animation = stackAnimation()
            ) { child ->
                when (val activeChild = child.instance) {
                    is RootComponent.Child.Main -> MainScreenWrapper(activeChild.component)
                    is RootComponent.Child.Setup -> SetupScreen(activeChild.component)
                }
            }
        }
    }
}

@Composable
private fun MainScreenWrapper(component: MainComponent) {
    Column {
        Row(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .height(IntrinsicSize.Min)
        ) {
            val child by component.page.subscribeAsState()
            val nestedChild by when (val instance = child.active.instance) {
                is MainComponent.Child.MasterDetail -> instance.component.page.subscribeAsState()
                else -> mutableStateOf(null)
            }

            TextButton(
                text = stringResource(Res.string.tab_explore),
                onClick = component::navigateToExploreAddOns,
                modifier = Modifier
                    .padding(end = 8.dp),
                enabled = child.active.instance !is MainComponent.Child.MasterDetail || nestedChild!!.active.instance !is MasterDetailComponent.Child.ExploreAddOns
            )

            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = lerp(Color(0xFF8ad3d3), Color.Black, 0.4F).copy(alpha = 0.45F)
            )

            TextButton(
                text = stringResource(Res.string.tab_manage),
                onClick = component::navigateToInstalledAddOns,
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                enabled = child.active.instance !is MainComponent.Child.MasterDetail || nestedChild!!.active.instance !is MasterDetailComponent.Child.InstalledAddOns
            )

            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = lerp(Color(0xFF8ad3d3), Color.Black, 0.4F).copy(alpha = 0.45F)
            )

            TextButton(
                text = stringResource(Res.string.tab_settings),
                onClick = component::navigateToSettings,
                modifier = Modifier
                    .padding(start = 8.dp),
                enabled = child.active.instance !is MainComponent.Child.Settings
            )
        }

        Column(
            modifier = Modifier
                .padding(end = 176.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1F, fill = true)
            ) {
                Children(
                    stack = component.page,
                    animation = stackAnimation()
                ) { child ->
                    when (val activeChild = child.instance) {
                        is MainComponent.Child.MasterDetail -> MainLayout(activeChild.component)
                        is MainComponent.Child.Settings -> SettingsScreen(activeChild.component)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val jobs by remember { component.jobs }.collectAsState()

                if (jobs.isEmpty()) {
                    LinearProgressIndicator(
                        progress = 1F,
                        modifier = Modifier
                            .height(7.dp)
                            .weight(1F, fill = true),
                        color = Color(0xFF8ad3d3),
                        backgroundColor = lerp(Color(0xFF8ad3d3), Color.Black, 0.4F),
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .height(7.dp)
                            .weight(1F, fill = true),
                        color = Color(0xFF8ad3d3),
                        backgroundColor = lerp(Color(0xFF8ad3d3), Color.Black, 0.4F),
                    )
                }

                EmphasisButton(
                    onClick = component::play,
                    modifier = Modifier
                        .width(182.dp),
                    enabled = jobs.isEmpty()
                ) {
                    Text(
                        text = stringResource(Res.string.game_start),
                        fontSize = 24.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedSocialsIconButton(
                    resource = Res.drawable.icon_discord,
                    onClickLabel = stringResource(Res.string.social_discord_tooltip),
                    onClick = { component.openLink(BuildConfig.DISCORD_URL) },
                    modifier = Modifier.size(14.dp)
                )

                OutlinedSocialsIconButton(
                    resource = Res.drawable.icon_github,
                    onClickLabel = stringResource(Res.string.social_github_tooltip),
                    onClick = { component.openLink(BuildConfig.GITHUB_URL) },
                    modifier = Modifier.size(14.dp)
                )

                Text(
                    text = stringResource(Res.string.app_disclaimer),
                    color = Color(0xFF8ad3d3).copy(alpha = 0.6F),
                    fontSize = 10.sp
                )

                Spacer(Modifier.weight(1F, fill = true))

                Text(
                    text = LocalApplicationInfo.current.version,
                    color = Color(0xFF8ad3d3).copy(alpha = 0.6F),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MainLayout(
    component: MasterDetailComponent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.White),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(270.dp),
            elevation = 8.dp
        ) {
            Children(
                stack = component.page,
                animation = stackAnimation()
            ) { child ->
                when (val activeChild = child.instance) {
                    is MasterDetailComponent.Child.ExploreAddOns -> ExploreAddOnsMaster(activeChild.component)
                    is MasterDetailComponent.Child.InstalledAddOns -> ManageAddOnsMaster(activeChild.component)
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(520.dp),
            color = Color.White,
            elevation = 8.dp
        ) {
            Children(
                stack = component.page,
                animation = stackAnimation()
            ) { child ->
                when (val activeChild = child.instance) {
                    is MasterDetailComponent.Child.ExploreAddOns -> ExploreAddOnsDetails(activeChild.component)
                    is MasterDetailComponent.Child.InstalledAddOns -> ManageAddOnsDetails(activeChild.component)
                }
            }
        }
    }
}