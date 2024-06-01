package com.gw2tb.manager.configuration

import com.gw2tb.manager.model.LocalStorage
import com.gw2tb.manager.model.TempDirectoryLayout
import kotlinx.coroutines.flow.Flow

/**
 * Provides the configuration for the manager.
 */
interface ConfigurationProvider {

    /** The PC-specific local storage of the manager. */
    val localStorage: Flow<LocalStorage>

    // TODO implement shared "settings" that contain release channel preferences

    /** The layout of the temporary directory. */
    val tempDirectoryLayout: TempDirectoryLayout

}