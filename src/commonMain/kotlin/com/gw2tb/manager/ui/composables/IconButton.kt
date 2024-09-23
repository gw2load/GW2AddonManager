package com.gw2tb.manager.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import java.awt.Cursor

//@Composable
//fun SocialsIconButton(
//    resource: DrawableResource,
//    onClickLabel: String,
//    onClick: () -> Unit
//) {
//    val interactionSource = remember { MutableInteractionSource() }
//    val isHovered by interactionSource.collectIsHoveredAsState()
//
//    val color = if (isHovered) {
//        lerp(Color(0xFF8ad3d3), Color.White, 0.6F)
//    } else {
//        Color(0xFF8ad3d3)
//    }
//
//    Icon(
//        painter = painterResource(resource),
//        contentDescription = null,
//        modifier = Modifier
//            .padding(4.dp)
//            .size(24.dp)
//            .clickable(
//                interactionSource = interactionSource,
//                indication = null,
//                onClickLabel = onClickLabel,
//                role = Role.Button,
//                onClick = onClick
//            )
//            .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
//        tint = color
//    )
//}

@Composable
fun OutlinedSocialsIconButton(
    resource: DrawableResource,
    onClickLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val color = if (isHovered) {
        lerp(Color(0xFF8ad3d3), Color.White, 0.6F)
    } else {
        Color(0xFF8ad3d3)
    }

    Icon(
        painter = painterResource(resource),
        contentDescription = null,
        modifier = Modifier
            .border(width = Dp.Hairline, color)
            .padding(2.dp)
            .then(modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick
            )
            .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        tint = color
    )
}