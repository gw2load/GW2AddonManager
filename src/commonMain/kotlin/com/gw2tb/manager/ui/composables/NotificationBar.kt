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
package com.gw2tb.manager.ui.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.exceptions.AddOnManifestException
import com.gw2tb.manager.exceptions.ManagerManifestException
import com.gw2tb.manager.exceptions.UnexpectedException
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.exception_addon_manifest_slug
import com.gw2tb.manager.gw2addonmanager.generated.resources.exception_manager_manifest_slug
import com.gw2tb.manager.gw2addonmanager.generated.resources.exception_unexpected_slug
import com.gw2tb.manager.gw2addonmanager.generated.resources.notification_addon_updates_available
import com.gw2tb.manager.gw2addonmanager.generated.resources.notification_duplicate_installations
import com.gw2tb.manager.gw2addonmanager.generated.resources.notification_manager_update_available
import com.gw2tb.manager.gw2addonmanager.generated.resources.notification_migrations_possible
import com.gw2tb.manager.gw2addonmanager.generated.resources.notification_missing_addon_dependencies
import com.gw2tb.manager.gw2addonmanager.generated.resources.notification_more
import com.gw2tb.manager.model.notifications.Notification
import com.gw2tb.manager.model.notifications.NotificationAddOnUpdatesAvailable
import com.gw2tb.manager.model.notifications.NotificationDuplicateInstallation
import com.gw2tb.manager.model.notifications.NotificationException
import com.gw2tb.manager.model.notifications.NotificationManagerUpdateAvailable
import com.gw2tb.manager.model.notifications.NotificationMigrationPossible
import com.gw2tb.manager.model.notifications.NotificationMissingAddOnDependencies
import com.gw2tb.manager.model.notifications.Urgency
import com.gw2tb.manager.ui.theme.ManagerColors
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import java.awt.Cursor

private const val DISPLAYED_NOTIFICATIONS = 3

@Composable
fun NotificationBar(
    notifications: StateFlow<List<Notification>>,
    onNotificationClick: (Notification) -> Unit,
    modifier: Modifier = Modifier
) {
    @Suppress("NAME_SHADOWING")
    val notifications by notifications.collectAsState()

    var offset by remember(notifications) { mutableStateOf(0) }

    val notificationsSequence = sequence {
        while (true) {
            yieldAll(notifications)
        }
    }

    val chipNotifications = when {
        notifications.size <= DISPLAYED_NOTIFICATIONS -> notifications
        else -> notificationsSequence.drop(offset).take(DISPLAYED_NOTIFICATIONS).toList()
    }

    val overflow = notifications.size - DISPLAYED_NOTIFICATIONS
    val overflowNotifications = when {
        overflow <= 0 -> emptyList()
        else -> notificationsSequence.drop(overflow + DISPLAYED_NOTIFICATIONS).take(overflow).toList()
    }

    /* To avoid animation flicker we force recomposition of the entire component when the locale changes. */
    key(LocalAppLocaleIso.current) {
        LazyRow(
            modifier = modifier
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = chipNotifications,
                key = {
                    /* This works because we only ever emit zero or one notification per type. */
                    it::class
                }
            ) { notification ->
                NotificationChip(
                    notification = notification,
                    onClick = { onNotificationClick(notification) },
                    modifier = Modifier
                        .animateItem()
                )
            }

            if (overflowNotifications.isNotEmpty()) {
                item(key = "overflow-indicator") {
                    OutlinedButton(
                        onClick = {
                            offset = (offset + DISPLAYED_NOTIFICATIONS) % notifications.size
                        }
                    ) {
                        Text(stringResource(Res.string.notification_more, overflowNotifications.size))
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationChip(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseColor = when (notification.urgency) {
        Urgency.Informational -> ManagerColors.PositiveHighlight
        Urgency.Warning -> ManagerColors.WarnHighlight
        Urgency.Error, Urgency.Critical -> ManagerColors.NegativeHighlight
    }

    val darkColor = lerp(baseColor, Color.Black, 0.4F)

    Row(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            .border(1.dp, color = lerp(baseColor, Color.Black, 0.4F))
    ) {
        Row(
            modifier = Modifier
                .background(brush = Brush.verticalGradient(listOf(lerp(baseColor, Color.Black, 0.4F), baseColor)))
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides darkColor,
                LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 10.sp),
            ) {
                Icon(
                    imageVector = when (notification.urgency) {
                        Urgency.Informational -> Icons.Outlined.Info
                        Urgency.Warning, Urgency.Error, Urgency.Critical -> Icons.Outlined.ReportProblem
                    },
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .background(brush = Brush.verticalGradient(listOf(Color.White, lerp(baseColor, Color.White, 0.5F))))
                .padding(4.dp),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides darkColor,
                LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 10.sp),
            ) {
                Text(
                    text = when (notification) {
                        is NotificationAddOnUpdatesAvailable -> stringResource(Res.string.notification_addon_updates_available)
                        is NotificationDuplicateInstallation -> stringResource(Res.string.notification_duplicate_installations)
                        is NotificationException -> when (notification.exception) {
                            is AddOnManifestException -> stringResource(Res.string.exception_addon_manifest_slug)
                            is ManagerManifestException -> stringResource(Res.string.exception_manager_manifest_slug)
                            is UnexpectedException -> stringResource(Res.string.exception_unexpected_slug)
                        }
                        is NotificationManagerUpdateAvailable -> stringResource(Res.string.notification_manager_update_available)
                        is NotificationMigrationPossible -> stringResource(Res.string.notification_migrations_possible)
                        is NotificationMissingAddOnDependencies -> stringResource(Res.string.notification_missing_addon_dependencies)
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
