package com.gw2tb.manager.services

import com.gw2tb.manager.model.LocalConfiguration
import com.gw2tb.manager.model.TempDirectoryLayout
import kotlinx.coroutines.flow.Flow

/**
 * The configuration services provides access to various configurations for the manager.
 *
 * The [localConfiguration] provides access to the local (i.e. PC-specific) configuration which contains information
 * such as the path to the game installation directory.
 *
 * @author  Leon Linhart
 */
interface ConfigurationService {

    /** The PC-specific local storage of the manager. */
    val localConfiguration: Flow<LocalConfiguration?>

    // TODO implement shared "settings" that contain release channel preferences

    /** The layout of the temporary directory. */
    val tempDirectoryLayout: TempDirectoryLayout

    fun isValid(localConfiguration: LocalConfiguration): Boolean {
        if (localConfiguration.selectedGameDirectory == null) return false
        return true
    }

    fun save(localConfiguration: LocalConfiguration)

}