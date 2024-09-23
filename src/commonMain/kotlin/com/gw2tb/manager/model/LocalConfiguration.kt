package com.gw2tb.manager.model

import com.gw2tb.manager.util.serialization.PathSerializer
import kotlinx.serialization.Serializable
import java.nio.file.Path

/**
 * The local storage of the manager contains PC-specific information (such as the currently selected game directory).
 *
 * @param selectedGameDirectory the currently selected game directory
 * @param gameDirectories       a list of known or previously selected game directories
 */
@Serializable
data class LocalConfiguration(
    val selectedGameDirectory: @Serializable(with = PathSerializer::class) Path? = null,
    val gameDirectories: List<@Serializable(with = PathSerializer::class) Path>
)