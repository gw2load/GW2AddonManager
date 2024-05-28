package com.gw2tb.manager.model

import androidx.compose.runtime.Immutable
import java.nio.file.Path

/**
 * Represents a locally installed add-on.
 *
 * @param kind      the kind of the add-on
 * @param name      the name of the add-on
 * @param path      the path to the add-on binary
 * @param isEnabled whether the add-on is enabled
 */
@Immutable
data class LocalAddOn(
    val kind: Kind,
    val name: String,
    val path: Path,
    val isEnabled: Boolean,
) {

    /** A kind of add-on. */
    enum class Kind {
        /** An _addon-loader_ (legacy) add-on. */
        ADDON_LOADER,
        /** A _GW2Load_ add-on. */
        GW2_LOAD
    }

}