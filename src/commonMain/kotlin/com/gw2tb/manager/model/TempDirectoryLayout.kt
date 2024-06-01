package com.gw2tb.manager.model

import java.nio.file.Path

/**
 * The configuration of the temporary directory used by the manager for downloads.
 *
 * @param directory     the root path of the temporary directory
 * @param gw2LoadPath   the path to download GW2Load to
 */
data class TempDirectoryLayout(
    val directory: Path,
    val gw2LoadPath: Path
)