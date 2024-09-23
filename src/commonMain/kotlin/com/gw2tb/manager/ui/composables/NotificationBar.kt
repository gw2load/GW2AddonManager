package com.gw2tb.manager.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.model.Notification
import kotlinx.coroutines.flow.StateFlow

// TODO This is not fully done yet

@Composable
fun NotificationBar(
    notifications: StateFlow<List<Notification>>,
    onNotificationClick: (Notification) -> Unit,
    modifier: Modifier = Modifier
) {
    @Suppress("NAME_SHADOWING")
    val notifications by notifications.collectAsState()

    var currentNotification by remember { mutableStateOf(0) }
    val interactionSource = remember { MutableInteractionSource() }

    AnimatedVisibility(
        visible = notifications.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val notification = notifications[currentNotification]

            val baseColor = when (notification.urgency) {
                Notification.Urgency.INFO -> Color(0xFF8ad3d3)
                Notification.Urgency.WARNING -> lerp(Color.Yellow, Color.Black, 0.4F)
                Notification.Urgency.REQUIRED -> Color.Red
            }

            TextButton(
                text = {
                    Text("Add-On updates available. Click to update all.")
                },
                onClick = { onNotificationClick(notification) },
                modifier = Modifier
                    .alignBy(FirstBaseline),
                color = lerp(Color.Yellow, Color.Black, 0.55F),
                idleColor = baseColor,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Previous",
                        modifier = Modifier.size(14.dp)
                    )
                }
            )

            if (notifications.size > 1) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable {

                        }
                )

                Text(
                    text = "1/3",
                    modifier = Modifier
                        .alignBy(FirstBaseline),
                    fontSize = 10.sp
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable {

                        }
                )
            }
        }
    }
}