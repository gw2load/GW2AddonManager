package com.gw2tb.manager.ui.composables

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.setting_game_directory_change
import com.gw2tb.manager.gw2addonmanager.generated.resources.setting_game_directory_label
import com.gw2tb.manager.gw2addonmanager.generated.resources.setup_select_game_directory_title
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Path

@Composable
fun GameDirectoryPicker(
    selectedGameDirectory: Path?,
    selectGameDirectory: (Path) -> Unit
) {
    Column {
        val directoryPickerLauncher = rememberDirectoryPickerLauncher(
            title = stringResource(Res.string.setup_select_game_directory_title),
            initialDirectory = null,
        ) { directory ->
            if (directory != null) {
                selectGameDirectory(Path.of(directory.path!!))
            }
        }

        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()

        val baseColor = lerp(Color(0xFF8ad3d3), Color.Black, 0.4F)

        BasicTextField(
            value = selectedGameDirectory?.toString() ?: "",
            onValueChange = {},
            readOnly = true,
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black.copy(alpha = ContentAlpha.disabled)
            ),
            singleLine = true,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.setting_game_directory_label),
                        color = if (isFocused) baseColor else Color.Black.copy(ContentAlpha.medium),
                        fontSize = 12.sp
                    )

                    innerTextField()

                    Divider(
                        color = if (isFocused) baseColor else Color.Black.copy(ContentAlpha.medium),
                        thickness = 2.dp
                    )
                }
            }
        )

        TextButton(
            text = stringResource(Res.string.setting_game_directory_change),
            onClick = { directoryPickerLauncher.launch() }
        )
    }
}