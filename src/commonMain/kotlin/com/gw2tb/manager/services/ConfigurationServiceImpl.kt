/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024 Leon Linhart
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
package com.gw2tb.manager.services

import com.gw2tb.manager.model.LocalConfiguration
import com.gw2tb.manager.model.TempDirectoryLayout
import com.gw2tb.manager.util.watchFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.*

fun ConfigurationService(
    localAppDataDirectory: Path,
    mainContext: CoroutineContext
): ConfigurationService {
    // 1. Resolve the local configuration path (for PC-specific information)
    val localConfigurationPath = localAppDataDirectory.resolve("config.json")

    // 2. Resolve the tmp directory
    val tmpDirsPath = try {
        System.getProperty("java.io.tmpdir")?.let(Path::of)!!
    } catch (e: InvalidPathException) {
        throw IllegalStateException("Invalid java.io.tmpdir path", e)
    }

    val tmpDir = tmpDirsPath.resolve("GW2AddOnManager")

    if (!Files.isDirectory(tmpDir)) {
        Files.createDirectories(tmpDir)
    }

    return ConfigurationServiceImpl(
        localConfigurationPath = localConfigurationPath,
        tempDirectoryLayout = TempDirectoryLayout(
            directory = tmpDir,
            gw2LoadPath = tmpDir.resolve("msimg32.dll"),
            installerPath = tmpDir.resolve("installer.msi")
        ),
        mainContext = mainContext
    )
}

private class ConfigurationServiceImpl(
    private val localConfigurationPath: Path,
    override val tempDirectoryLayout: TempDirectoryLayout,
    mainContext: CoroutineContext,
) : ConfigurationService {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(ConfigurationServiceImpl::class.java)
    }

    private val coroutineScope = CoroutineScope(mainContext + SupervisorJob())

    private val json = Json {
        prettyPrint = true
    }

    override val localConfiguration: Flow<LocalConfiguration?> =
        localConfigurationPath
            .watchFile()
            .map { loadLocalConfiguration(localConfigurationPath) }
            .flowOn(Dispatchers.IO)
            .distinctUntilChanged()
            .conflate()
            .shareIn(scope = coroutineScope, started = SharingStarted.Eagerly, replay = 1)

    fun loadLocalConfiguration(path: Path): LocalConfiguration? {
        log.info("Loading local configuration from '{}'", path)

        if (!path.isRegularFile()) {
            log.info("Local configuration file does not exist")
            return null
        }

        return try {
            json.decodeFromString(path.readText())
        } catch (e: SerializationException) {
            log.error("Failed to load local configuration due to a deserialization error", e)
            null
        }
    }

    override fun save(localConfiguration: LocalConfiguration) {
        log.info("Saving local configuration to '{}'", localConfigurationPath)

        if (!localConfigurationPath.parent.isDirectory()) {
            log.debug("Creating parent directories for '{}'", localConfigurationPath)
            Files.createDirectories(localConfigurationPath.parent)
        }

        localConfigurationPath.writeText(json.encodeToString(localConfiguration))
    }

}