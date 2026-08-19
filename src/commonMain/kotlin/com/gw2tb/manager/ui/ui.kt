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
package com.gw2tb.manager.ui

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.gw2tb.manager.gw2addonmanager.generated.resources.*
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.internal.BuildConfig
import com.gw2tb.manager.ui.composables.*
import com.gw2tb.manager.ui.screens.confirm.ConfirmActionPlan
import com.gw2tb.manager.ui.screens.details.AddOnDetails
import com.gw2tb.manager.ui.screens.exception.Exception
import com.gw2tb.manager.ui.screens.explore.ExploreAddOns
import com.gw2tb.manager.ui.screens.manage.ManageAddOns
import com.gw2tb.manager.ui.screens.settings.SettingsScreen
import com.gw2tb.manager.ui.screens.setup.SetupScreen
import com.gw2tb.manager.ui.theme.ManagerColors
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
                            modifier = Modifier
                                .weight(1F, fill = true),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LanguageSelector(
                                selectLocale = selectLocale
                            )

                            val childStack by component.page.subscribeAsState()
                            if (childStack.active.instance !is RootComponent.Child.Setup) {
                                NotificationBar(
                                    notifications = component.notifications,
                                    onNotificationClick = component::onNotificationClick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.icon_minimize),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color = ManagerColors.Primary)
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
                                    .background(color = ManagerColors.Primary)
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
                text = {
                    TextWithFixedWeightWidth(
                        weight = if (child.active.instance !is MainComponent.Child.MasterDetail || nestedChild!!.active.instance !is MasterDetailComponent.Child.ExploreAddOns) null else FontWeight.Medium,
                        maxWeight = FontWeight.Medium
                    ) {
                        Text(stringResource(Res.string.tab_explore))
                    }
                },
                onClick = component::navigateToExploreAddOns,
                modifier = Modifier
                    .padding(end = 8.dp),
                enabled = child.active.instance !is MainComponent.Child.MasterDetail || nestedChild!!.active.instance !is MasterDetailComponent.Child.ExploreAddOns,
                onClickLabel = stringResource(Res.string.tab_explore),
                role = Role.Tab
            )

            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = lerp(ManagerColors.Primary, Color.Black, 0.4F).copy(alpha = 0.45F)
            )

            TextButton(
                text = {
                    TextWithFixedWeightWidth(
                        weight = if (child.active.instance !is MainComponent.Child.MasterDetail || nestedChild!!.active.instance !is MasterDetailComponent.Child.InstalledAddOns) null else FontWeight.Medium,
                        maxWeight = FontWeight.Medium
                    ) {
                        Text(stringResource(Res.string.tab_manage))
                    }
                },
                onClick = component::navigateToInstalledAddOns,
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                enabled = child.active.instance !is MainComponent.Child.MasterDetail || nestedChild!!.active.instance !is MasterDetailComponent.Child.InstalledAddOns,
                onClickLabel = stringResource(Res.string.tab_manage),
                role = Role.Tab
            )

            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = lerp(ManagerColors.Primary, Color.Black, 0.4F).copy(alpha = 0.45F)
            )

            TextButton(
                text = {
                    TextWithFixedWeightWidth(
                        weight = if (child.active.instance !is MainComponent.Child.Settings) null else FontWeight.Medium,
                        maxWeight = FontWeight.Medium
                    ) {
                        Text(stringResource(Res.string.tab_settings))
                    }
                },
                onClick = component::navigateToSettings,
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                enabled = child.active.instance !is MainComponent.Child.Settings,
                onClickLabel = stringResource(Res.string.tab_settings),
                role = Role.Tab
            )

            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = lerp(ManagerColors.Primary, Color.Black, 0.4F).copy(alpha = 0.45F)
            )

            TextButton(
                text = {
                    TextWithFixedWeightWidth(
                        weight = null,
                        maxWeight = FontWeight.Medium
                    ) {
                        Text(stringResource(Res.string.tab_help))
                    }
                },
                onClick = component::navigateToHelp,
                modifier = Modifier
                    .padding(start = 8.dp),
                enabled = true,
                onClickLabel = stringResource(Res.string.tab_help),
                role = Role.Tab
            )
        }

        Column(
            modifier = Modifier
                .padding(end = 176.dp)
        ) {
            val stack by component.page.subscribeAsState()
            val targetElevation = if (stack.active.instance is MainComponent.Child.MasterDetail) 8.dp else 0.dp
            val animatedElevation by animateDpAsState(targetValue = targetElevation)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F, fill = true),
                color = if (stack.active.instance is MainComponent.Child.MasterDetail) Color.White else Color.Transparent,
                elevation = animatedElevation
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

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val (progress, button, checkbox) = createRefs()
                val jobs by remember { component.jobs }.collectAsState()

                if (jobs.isEmpty()) {
                    LinearProgressIndicator(
                        progress = 1F,
                        modifier = Modifier
                            .constrainAs(progress) {
                                start.linkTo(parent.start)
                                end.linkTo(button.start, margin = 12.dp)
                                centerVerticallyTo(button)

                                width = Dimension.fillToConstraints
                            }
                            .height(7.dp),
                        color = ManagerColors.Primary,
                        backgroundColor = lerp(ManagerColors.Primary, Color.Black, 0.4F),
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .constrainAs(progress) {
                                start.linkTo(parent.start)
                                end.linkTo(button.start, margin = 12.dp)
                                centerVerticallyTo(button)

                                width = Dimension.fillToConstraints
                            }
                            .height(7.dp),
                        color = ManagerColors.Primary,
                        backgroundColor = lerp(ManagerColors.Primary, Color.Black, 0.4F),
                    )
                }

                val canPlay by component.canPlay.collectAsState()

                EmphasisButton(
                    onClick = component::play,
                    modifier = Modifier
                        .constrainAs(button) {
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                        .width(182.dp),
                    enabled = canPlay
                ) {
                    Text(
                        text = stringResource(Res.string.game_start),
                        fontSize = 24.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .constrainAs(checkbox) {
                            end.linkTo(progress.end)
                            bottom.linkTo(progress.top, margin = 4.dp)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.setting_auto_update_label),
                        color = lerp(ManagerColors.Primary, Color.White, 0.4F),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val areAutoUpdatesEnabled by component.areAutoUpdatesEnabled.collectAsState()

                    Checkbox(
                        checked = areAutoUpdatesEnabled,
                        onClick = { component.setAutoUpdatesEnabled(!areAutoUpdatesEnabled) },
                        color = ManagerColors.Primary,
                        hoverColor = lerp(ManagerColors.Primary, Color.White, 0.4F),
                        padding = PaddingValues(start = 4.dp, top = 4.dp, end = 0.dp, bottom = 4.dp)
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
                    color = ManagerColors.Primary.copy(alpha = 0.6F),
                    fontSize = 10.sp
                )

                Spacer(Modifier.weight(1F, fill = true))

                Text(
                    text = LocalApplicationInfo.current.version,
                    color = ManagerColors.Primary.copy(alpha = 0.6F),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
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
    Children(
        stack = component.page,
        modifier = modifier,
        animation = stackAnimation()
    ) { child ->
        when (val activeChild = child.instance) {
            is MasterDetailComponent.Child.AddOnDetails -> AddOnDetails(activeChild.component)
            is MasterDetailComponent.Child.Confirm -> ConfirmActionPlan(activeChild.component)
            is MasterDetailComponent.Child.Exception -> Exception(activeChild.component)
            is MasterDetailComponent.Child.ExploreAddOns -> ExploreAddOns(activeChild.component)
            is MasterDetailComponent.Child.InstalledAddOns -> ManageAddOns(activeChild.component)
        }
    }
}
