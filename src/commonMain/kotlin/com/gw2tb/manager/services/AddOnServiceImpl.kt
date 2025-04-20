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

import com.gw2tb.manager.discoverer.AddOnDiscoverer
import com.gw2tb.manager.discoverer.Gw2LoadAddOnDiscoverer
import com.gw2tb.manager.discoverer.Gw2LoadDiscoverer
import com.gw2tb.manager.discoverer.LegacyAddOnDiscoverer
import com.gw2tb.manager.model.*
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.catalog.isMatching
import com.gw2tb.manager.model.catalog.toUpdateFor
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.repository.AddOnRepository
import com.gw2tb.manager.util.watchDirectory
import com.sun.nio.file.ExtendedWatchEventModifier
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.zip.ZipFile
import kotlin.coroutines.CoroutineContext

fun AddOnService(
    addOnRepository: AddOnRepository,
    configurationService: ConfigurationService,
    jobService: JobService,
    mainContext: CoroutineContext
): AddOnService = AddOnServiceImpl(
    addOnRepository = addOnRepository,
    configurationService = configurationService,
    jobService = jobService,
    mainContext = mainContext
)

private class AddOnServiceImpl(
    private val addOnRepository: AddOnRepository,
    configurationService: ConfigurationService,
    private val jobService: JobService,
    mainContext: CoroutineContext
) : AddOnService {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(AddOnServiceImpl::class.java)
    }

    private val coroutineScope = CoroutineScope(mainContext + SupervisorJob())

    private val gw2LoadDiscoverer = Gw2LoadDiscoverer()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val addOnDiscoverers: Flow<List<AddOnDiscoverer>> = configurationService.localConfiguration
        .mapNotNull { it?.selectedGameDirectory }
        .distinctUntilChanged()
        .flatMapLatest { selectedGameDirectory ->
            selectedGameDirectory
                .watchDirectory(
                    modifiers = arrayOf(ExtendedWatchEventModifier.FILE_TREE),
                    filter = { path, _ -> path.fileName.toString() == "msimg32.dll" }
                )
                .map { selectedGameDirectory }
        }
        .transformLatest { gameDirectory ->
            var prevAddOnDiscoverer: Gw2LoadAddOnDiscoverer? = null

            suspend fun scanAndEmitAddOns() {
                val gw2LoadInstances = gw2LoadDiscoverer.getAddOns(gameDirectory)

                val addOnDiscover: Gw2LoadAddOnDiscoverer? = try {
                    when (gw2LoadInstances.size) {
                        0 -> {
                            /*
                             * When we cannot find any GW2Load instance in the game directory, we have to fall back to
                             */
                            val storedGw2LoadPath = configurationService.tempDirectoryLayout.gw2LoadPath
                            if (Files.isRegularFile(storedGw2LoadPath) && Gw2LoadAddOnDiscoverer.isValid(
                                    storedGw2LoadPath
                                )
                            ) {
                                withContext(Dispatchers.IO) {
                                    Files.copy(storedGw2LoadPath, gameDirectory.resolve("msimg32.dll"))
                                }
                            } else {
                                // TODO Download the latest GW2Load version
                            }

                            throw IllegalArgumentException("No GW2Load instance found")
                        }

                        1 -> {
                            log.debug("Found a single instance of GW2Load")
                            Gw2LoadAddOnDiscoverer(libraryPath = gw2LoadInstances.first().path)
                        }

                        else -> {
                            log.warn("Found multiple instances of GW2Load, using the first one")
                            TODO()
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    TODO()
                }

                prevAddOnDiscoverer?.close()
                prevAddOnDiscoverer = addOnDiscover

                log.debug("Emitting add-on discoverers")
                emit(buildList {
                    /*
                     * We don't want to show GW2Load itself in the list of add-ons at this time, but we could easily change that
                     * by uncommenting the following line.
                     * (The reason for this is that the loader is unnecessary cognitive load that regular users should not have
                     * to deal with. So, we hide it as good as possible.)
                     */
//                    add(gw2LoadDiscoverer)

                    /*
                     * We always want to show the legacy add-on discoverer because it's required to provide clean migration paths.
                     */
                    add(LegacyAddOnDiscoverer())
                    if (addOnDiscover != null) add(addOnDiscover)
                })
            }

            scanAndEmitAddOns()
        }
        .distinctUntilChanged()

    private val _addOnListings = MutableStateFlow(emptyList<AddOnListing>())

    override val addOnListings: Flow<List<AddOnListing>> = flow {
        emit(addOnRepository.getAddOnListings())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val localAddOns: Flow<List<LocalAddOn>> = configurationService.localConfiguration
        .mapNotNull { it?.selectedGameDirectory }
        .combine(addOnDiscoverers) { a, b -> a to b }
        .distinctUntilChanged()
        .flatMapLatest { (gameDirectory, discoverers) ->
            channelFlow {
                suspend fun emitAddOns() {
                    log.info("Discovering add-ons in game directory: {}", gameDirectory)

                    val localAddOns = discoverers.flatMap { it.getAddOns(gameDirectory) }
                    log.info("Discovered add-ons: {}", localAddOns)

                    try {
                        send(localAddOns)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }

                    log.info("Emitted add-ons")
                }

                log.info("Started watching game directory '{}' using discoverers: {}", gameDirectory, discoverers)

                emitAddOns()

                gameDirectory
                    .watchDirectory(modifiers = arrayOf(ExtendedWatchEventModifier.FILE_TREE))
                    .collectLatest { emitAddOns() }
            }
        }
        .distinctUntilChanged()
//        .conflate()
        .shareIn(scope = coroutineScope, started = SharingStarted.Eagerly, replay = 1)

    override val availableUpdates: Flow<List<AvailableAddOnUpdate>> = localAddOns
        .combine(addOnListings) { localAddOns, addOnListings ->
            addOnListings.mapNotNull { listing ->
                val addOn = localAddOns.find { listing isMatching it } ?: return@mapNotNull null
                listing.toUpdateFor(addOn)
            }
        }
        .distinctUntilChanged()
        .conflate()

    override suspend fun disable(addOn: LocalAddOn) {
        jobService.runJob(
            localAddOns = listOf(addOn)
        ) {
            withContext(Dispatchers.IO) {
                Files.move(addOn.path, addOn.path.resolveSibling("${addOn.path.fileName}.disabled"))
            }
        }
    }

    override suspend fun enable(addOn: LocalAddOn): Unit = withContext(Dispatchers.IO) {
        jobService.runJob(
            localAddOns = listOf(addOn)
        ) {
            Files.move(addOn.path, addOn.path.resolveSibling(addOn.path.fileName.toString().removeSuffix(".disabled")))
        }
    }

    private enum class DownloadType { Archive, Dll }

    private suspend fun getDependencies(listing: AddOnListing): Set<AddOnListing> {
        val addOnListings = addOnListings.last()

        return buildSet {
            val queue = ArrayDeque(elements = listOf(listing))
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (add(current)) {
                    for (dependency in current.dependencies) {
                        val dependencyListing = addOnListings.find { it.id == dependency } ?: throw IllegalStateException("Could not find dependency: $dependency")
                        queue.add(dependencyListing)
                    }
                }
            }
        }
    }

    override suspend fun install(listing: AddOnListing, gameDirectory: Path) {
        if (listing.download == null) {
            throw IllegalArgumentException("Listing does not have a download URL")
        }

        log.info("Installing add-on: {}", listing.addOnName)

        val addOnListings = addOnListings.last()

        jobService.runJob(
            addOnListings = listOf(listing)
        ) {
            for (dependency in listing.dependencies) {
                val dependencyListing = addOnListings.find { it.id == dependency } ?: throw IllegalStateException("Could not find dependency: $dependency")
                install(dependencyListing, gameDirectory)
            }

            val targetDirectory = when (listing.installMode) {
                AddOnListing.InstallMode.Arc -> gameDirectory.resolve("addons/arcdps")
                AddOnListing.InstallMode.Gw2Load -> gameDirectory.resolve("addons/${listing.addOnName}")
            }

            val downloadType = when {
                listing.download.downloadUrl.endsWith(".zip") -> DownloadType.Archive
                listing.download.downloadUrl.endsWith(".dll") -> DownloadType.Dll
                else -> throw IllegalArgumentException("Unknown download type for URL: ${listing.download.downloadUrl}")
            }

            val targetFileName = when (downloadType) {
                DownloadType.Archive -> Url(listing.download.downloadUrl).segments.last()
                DownloadType.Dll -> when (listing.installMode) {
                    AddOnListing.InstallMode.Arc -> Url(listing.download.downloadUrl).segments.last()
                    AddOnListing.InstallMode.Gw2Load -> "gw2addon_${listing.addOnName}.dll"
                }
            }

            val downloadTargetPath = targetDirectory.resolve("$targetFileName.tmp")

            withContext(Dispatchers.IO) {
                Files.createDirectories(downloadTargetPath.parent)

                try {
                    FileChannel.open(downloadTargetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { outputChannel ->
                        addOnRepository.download(listing).use { inputChannel ->
                            outputChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE)
                        }
                    }

                    when (downloadType) {
                        DownloadType.Archive -> {
                            /*
                             * If we're dealing with an archive (.zip) download, we extract the contents of the archive into
                             * the target directory (overwriting any existing entries).
                             * However, we have to do some trickery here to make sure that we extract the files into the
                             * correct locations. Specifically, we handle two different cases:
                             * 1. The archive contains the addon (.dll) directly at the top-level, or
                             * 2. The archive contains a single top-level directory.
                             *
                             * In case 2., we ignore the directory and extract the contents of the directory directly into
                             * the target directory.
                             */
                            ZipFile(downloadTargetPath.toFile()).use { zipFile ->
                                val entries = zipFile.entries().toList()

                                /*
                                 * If there is no top-level entry that is not a directory and ends with ".dll", we require
                                 * exactly one top-level directory.
                                 */
                                val topLevelDirectory = if (entries.none { !it.isDirectory && Path.of(it.name).parent == null && it.name.endsWith(".dll") }) {
                                    entries.singleOrNull { it.isDirectory && Path.of(it.name).parent == null } ?: throw IllegalStateException("Could not find top-level directory or add-on in archive")
                                } else {
                                    null
                                }

                                entries.forEach { entry ->
                                    if (entry == topLevelDirectory) return@forEach

                                    val entryPath = targetDirectory.resolve(Path.of(entry.name).let {
                                        if (topLevelDirectory != null) it.subpath(1, it.nameCount) else it
                                    })

                                    if (entry.isDirectory) {
                                        /* Make sure to create empty directories here (just in case that's required for the add-on). */
                                        Files.createDirectories(entryPath)
                                    } else {
                                        Files.createDirectories(entryPath.parent)
                                        zipFile.getInputStream(entry).use { input ->
                                            Files.copy(input, entryPath, StandardCopyOption.REPLACE_EXISTING)
                                        }
                                    }
                                }
                            }
                        }
                        DownloadType.Dll -> {
                            /*
                             * If we're dealing with a binary (.dll) download, we simply move the freshly downloaded file
                             * into it's final destination (overwriting any previous file).
                             */
                            Files.move(downloadTargetPath, targetDirectory.resolve(targetFileName), StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                } finally {
                    Files.deleteIfExists(downloadTargetPath)
                }
            }
        }
    }

    override suspend fun uninstall(addOn: LocalAddOn) {
        jobService.runJob {
            withContext(Dispatchers.IO) {
                log.info("Uninstalling add-on: {}", addOn.name)

                try {
                    Files.delete(addOn.path)
                } catch (e: IOException) {
                    log.error("Failed to delete add-on: {}", addOn.name, e)
                }
            }
        }
    }

    override suspend fun refreshListings() {
        addOnRepository.invalidateCache()

        val addOnListings = addOnRepository.getAddOnListings()
        _addOnListings.emit(addOnListings)
    }

}