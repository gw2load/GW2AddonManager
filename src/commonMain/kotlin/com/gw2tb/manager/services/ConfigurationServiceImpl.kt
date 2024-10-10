package com.gw2tb.manager.services

import com.gw2tb.manager.model.LocalConfiguration
import com.gw2tb.manager.model.TempDirectoryLayout
import com.gw2tb.manager.util.watchFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.*

fun ConfigurationService(
    localAppDataDirectory: Path
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
    val storedGw2LoadPath = tmpDir.resolve("msimg32.dll")

    if (!Files.isDirectory(tmpDir)) {
        Files.createDirectories(tmpDir)
    }

    return ConfigurationServiceImpl(
        localConfigurationPath = localConfigurationPath,
        tempDirectoryLayout = TempDirectoryLayout(
            directory = tmpDir,
            gw2LoadPath = storedGw2LoadPath
        )
    )
}

private class ConfigurationServiceImpl(
    private val localConfigurationPath: Path,
    override val tempDirectoryLayout: TempDirectoryLayout
) : ConfigurationService {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(ConfigurationServiceImpl::class.java)
    }

    private val json = Json {
        prettyPrint = true
    }

    override val localConfiguration: Flow<LocalConfiguration?> =
        flow {
            emit(localConfigurationPath)
            emitAll(localConfigurationPath.watchFile().map { localConfigurationPath })
        }.map(::loadLocalConfiguration)
            .distinctUntilChanged()
            .conflate()
            .shareIn(scope = GlobalScope, started = SharingStarted.Eagerly, replay = 1)

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