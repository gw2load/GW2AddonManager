/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2025 Leon Linhart
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

import com.gw2tb.manager.AppInfo
import com.gw2tb.manager.discoverer.Gw2LoadDiscoverer
import com.gw2tb.manager.discoverer.loader.Loader
import com.gw2tb.manager.model.catalog.Download
import com.gw2tb.manager.repository.AddOnRepository
import com.gw2tb.manager.util.ReadWriteMutex
import com.gw2tb.manager.util.fileinfo.readAddOnFileInfo
import com.gw2tb.manager.util.watchDirectory
import com.gw2tb.manager.util.watchFile
import com.sun.nio.file.ExtendedWatchEventModifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.isRegularFile
import kotlin.time.Duration.Companion.milliseconds

fun LoaderService(
    addOnRepository: AddOnRepository,
    configurationService: ConfigurationService,
    appInfo: AppInfo,
    mainContext: CoroutineContext
): LoaderService = LoaderServiceImpl(
    addOnRepository = addOnRepository,
    configurationService = configurationService,
    appInfo = appInfo,
    mainContext = mainContext
)

class LoaderServiceImpl(
    private val addOnRepository: AddOnRepository,
    private val configurationService: ConfigurationService,
    private val appInfo: AppInfo,
    mainContext: CoroutineContext
) : LoaderService {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(LoaderServiceImpl::class.java)
    }

    private val coroutineScope = CoroutineScope(mainContext + SupervisorJob())

    @OptIn(ExperimentalAtomicApi::class)
    private val isRefreshingLoader = AtomicBoolean(false)

    private val tempLoaderRwLock = ReadWriteMutex()

    private val gw2LoadDiscoverer = Gw2LoadDiscoverer()

    private var latestLoader: Loader? = null

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalAtomicApi::class, FlowPreview::class)
    override val loader: Flow<Loader> = configurationService.localConfiguration
        .mapNotNull { it?.selectedGameDirectory }
        .flatMapLatest { gameDirectory ->
            gameDirectory
                .watchDirectory(
                    modifiers = arrayOf(ExtendedWatchEventModifier.FILE_TREE),
                    filter = { path, _ -> path.fileName.toString() == "msimg32.dll" || path.fileName.toString() == "msimg32.dll.disabled" }
                )
                .debounce(10.milliseconds)
                .map { gameDirectory }
                .catch { e ->
                    log.error("Failed to watch game directory for GW2Load", e)
                    emit(gameDirectory)
                }
        }
        .catch { e -> log.error("Failed to watch game directory for GW2Load", e) }
        .transformLatest { gameDirectory ->
            configurationService.tempDirectoryLayout.gw2LoadPath.watchFile()
                .collect {
                    /* First, we close the latest loader (which unloads the shared library). */
                    val latestLoader = latestLoader
                    latestLoader?.close()

                    /*
                     * If the loader is not currently being verified, we check if we have to copy the loader from the
                     * temporary directory into the game directory.
                     */
                    if (!isRefreshingLoader.load()) {
                        tempLoaderRwLock.withReadLock {
                            val tempLoaderPath = configurationService.tempDirectoryLayout.gw2LoadPath
                            if (tempLoaderPath.isRegularFile()) {
                                val tempLoaderAddOnFileInfo = tempLoaderPath.readAddOnFileInfo() ?: TODO()

                                if (latestLoader != null && (latestLoader.isBundled || latestLoader.localAddOn.version.fileVersion < tempLoaderAddOnFileInfo.version)) {
                                    try {
                                        log.info("Copying $tempLoaderPath to $tempLoaderAddOnFileInfo")
                                        Files.copy(tempLoaderPath, gameDirectory.resolve("msimg32.dll"), StandardCopyOption.REPLACE_EXISTING)
                                    } catch (e: Exception) {
                                        log.error("Error copying $tempLoaderPath to $gameDirectory", e)
                                    }
                                }
                            }
                        }
                    }

                    var gw2LoadInstances = gw2LoadDiscoverer.getAddOns(gameDirectory, emptyList())
                    emit(when (gw2LoadInstances.size) {
                        0 -> {
                            val applicationDir = appInfo.applicationDir ?: error("Application directory could not be identified")
                            gw2LoadInstances = gw2LoadDiscoverer.getAddOns(applicationDir.resolve("loader"), emptyList())

                            val localLoader = gw2LoadInstances.single()
                            log.info("Using bundled instance of GW2Load at '{}'", localLoader.path)
                            Loader(localLoader, isBundled = true)
                        }
                        1 -> {
                            val localLoader = gw2LoadInstances.single()
                            log.info("Found a single instance of GW2Load at '{}'", localLoader.path)
                            Loader(localLoader, isBundled = false)
                        }
                        else -> {
                            // TODO Revisit the selection behavior in edge-cases
                            log.warn("Found multiple instances of GW2Load, using the first one")
                            Loader(gw2LoadInstances.first(), isBundled = false)
                        }
                    }.also { this@LoaderServiceImpl.latestLoader = it })
                }
        }
        .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)

    init {
        coroutineScope.launch {
            verifyLoader()
        }
    }

    private suspend fun downloadLoader(download: Download) = withContext(Dispatchers.IO) {
        log.debug("Downloading new loader {}", download.version)

        FileChannel.open(configurationService.tempDirectoryLayout.gw2LoadPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { outputChannel ->
            addOnRepository.download(download).use { inputChannel ->
                outputChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE)
            }
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    override suspend fun verifyLoader() {
        if (!isRefreshingLoader.compareAndSet(expectedValue = false, newValue = true)) {
            log.debug("Loader validation is already running")
            return
        }

        log.debug("Verifying loader")

        try {
            tempLoaderRwLock.withWriteLock {
                val currentLoader = loader.first()
                val loaderManifestEntry = addOnRepository.getLoader() ?: return@withWriteLock

                val loaderRelease = loaderManifestEntry.release
                if (loaderRelease.version > currentLoader.localAddOn.version || currentLoader.isBundled) {
                    downloadLoader(loaderRelease)
                }
            }
        } finally {
            isRefreshingLoader.store(false)
        }
    }

}
