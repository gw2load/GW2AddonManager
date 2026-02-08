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

import com.gw2tb.manager.actions.Action
import com.gw2tb.manager.actions.ActionDisableAddOn
import com.gw2tb.manager.actions.ActionEnableAddOn
import com.gw2tb.manager.actions.ActionInstallAddOn
import com.gw2tb.manager.actions.ActionPlan
import com.gw2tb.manager.actions.ActionPlan.Stage
import com.gw2tb.manager.actions.ActionRenameAddOn
import com.gw2tb.manager.actions.ActionUninstallAddOn
import com.gw2tb.manager.actions.ActionUpdateAddOn
import com.gw2tb.manager.actions.OperationResult
import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.discoverer.AddOnDiscoverer
import com.gw2tb.manager.discoverer.LegacyAddOnLoaderDiscoverer
import com.gw2tb.manager.discoverer.ArcDpsAddOnDiscoverer
import com.gw2tb.manager.discoverer.Gw2LoadAddOnDiscoverer
import com.gw2tb.manager.discoverer.Gw2LoadDiscoverer
import com.gw2tb.manager.discoverer.LegacyAddOnDiscoverer
import com.gw2tb.manager.discoverer.LegacyArcDpsDiscoverer
import com.gw2tb.manager.model.*
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.catalog.isMatching
import com.gw2tb.manager.model.inspections.InspectionMigrationPossible
import com.gw2tb.manager.model.inspections.migrations.Migration
import com.gw2tb.manager.model.inspections.migrations.MigrationContext
import com.gw2tb.manager.model.inspections.migrations.Migrator
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.repository.AddOnRepository
import com.gw2tb.manager.util.watchDirectory
import com.sun.nio.file.ExtendedWatchEventModifier
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.io.IOException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.zip.ZipFile
import kotlin.collections.any
import kotlin.collections.flatten
import kotlin.collections.none
import kotlin.collections.toList
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

fun AddOnService(
    addOnRepository: AddOnRepository,
    configurationService: ConfigurationService,
    jobService: JobService,
    loaderService: LoaderService,
    mainContext: CoroutineContext
): AddOnService = AddOnServiceImpl(
    addOnRepository = addOnRepository,
    configurationService = configurationService,
    jobService = jobService,
    loaderService = loaderService,
    mainContext = mainContext
)

private class AddOnServiceImpl(
    private val addOnRepository: AddOnRepository,
    private val configurationService: ConfigurationService,
    private val jobService: JobService,
    private val loaderService: LoaderService,
    mainContext: CoroutineContext
) : AddOnService {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(AddOnServiceImpl::class.java)
    }

    private val coroutineScope = CoroutineScope(mainContext + SupervisorJob())

    private val addOnDiscoverers: Flow<List<AddOnDiscoverer>> = loaderService.loader
        .map { loader ->
            buildList {
                add(Gw2LoadDiscoverer())
                add(Gw2LoadAddOnDiscoverer(loader))
                add(LegacyAddOnLoaderDiscoverer())
                add(LegacyArcDpsDiscoverer())
                add(ArcDpsAddOnDiscoverer())
                add(LegacyAddOnDiscoverer())
            }
        }

    private val _addOnListings = MutableStateFlow(emptyList<AddOnListing>())
    override val addOnListings: Flow<List<AddOnListing>> = _addOnListings.asStateFlow()

    init {
        coroutineScope.launch {
            refresh()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override val localAddOns: Flow<List<LocalAddOn>> = configurationService.localConfiguration
        .mapNotNull { it?.selectedGameDirectory }
        .combine(addOnDiscoverers) { a, b -> a to b }
        .distinctUntilChanged()
        .flatMapLatest { (gameDirectory, discoverers) ->
            gameDirectory
                .watchDirectory(modifiers = arrayOf(ExtendedWatchEventModifier.FILE_TREE))
                /*
                 * Some actions such as deleting a directory or replacing a file may cause more than one event to be
                 * sent. We debounce those events over 10 ms to ensure we don't scan for add-ons too often.
                 */
                .debounce(10.milliseconds)
                .map {
                    /*
                     * It is possible that some add-ons support multiple loaders and are detected by multiple
                     * discoverers. To prevent this, we take the first add-on for each path. This requires that the
                     * discoverers have to run in the expected order.
                     */
                    buildList<LocalAddOn> {
                        for (discoverer in discoverers) {
                            val localAddOns = discoverer.getAddOns(gameDirectory, toList())
                            log.info("Found {} local add-ons using {}", localAddOns.size, discoverer::class.simpleName)

                            for (localAddOn in localAddOns) {
                                log.debug("Found local-addon: {}", localAddOn)

                                if (this.any { it.path == localAddOn.path }) {
                                    log.warn("Skipping local add-on '{}' that was already discovered", localAddOn.path)
                                    continue
                                }

                                add(localAddOn)
                            }
                        }
                    }
                }
        }
        .onStart { emit(emptyList()) }
        .shareIn(scope = coroutineScope, started = SharingStarted.Eagerly, replay = 1)

    override val installedAddOns: Flow<List<InstalledAddOn>> =
        localAddOns
            .combine(addOnListings) { localAddOns, addOnListings ->
                localAddOns
                    .map { localAddOn ->
                        val listing = addOnListings.find { it isMatching localAddOn }
                        InstalledAddOn(localAddOn, listing)
                    }
                    .sortedBy { (localAddOn, addOnListing) -> addOnListing?.addOnName ?: localAddOn.name }
            }
            .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    private suspend fun doDisableAddOn(localAddOn: LocalAddOn) {
        withContext(Dispatchers.IO) {
            log.info("Disabling add-on: {}", localAddOn.name)

            try {
                Files.move(localAddOn.path, localAddOn.path.resolveSibling("${localAddOn.path.fileName}.disabled"))
                log.debug("Successfully disabled add-on: {}", localAddOn)
            } catch (e: IOException) {
                log.error("Failed to disable add-on", e)
            }
        }
    }

    private suspend fun doEnableAddOn(localAddOn: LocalAddOn) {
        withContext(Dispatchers.IO) {
            log.info("Enabling add-on: {}", localAddOn.path)

            try {
                Files.move(localAddOn.path, localAddOn.path.resolveSibling(localAddOn.path.fileName.toString().removeSuffix(".disabled")))
                log.debug("Successfully enabled add-on: {}", localAddOn)
            } catch (e: IOException) {
                log.error("Failed to enable add-on", e)
            }
        }
    }

    private suspend fun doInstallAddOn(listing: AddOnListing) {
        log.info("Installing add-on: {}", listing.addOnName)
        downloadAddOn(listing)
    }

    private suspend fun doRenameAddOn(localAddOn: LocalAddOn, newFileName: String) {
        withContext(Dispatchers.IO) {
            try {
                Files.move(localAddOn.path, localAddOn.path.resolveSibling(newFileName))
            } catch (e: IOException) {
                log.error("Failed to rename add-on: {}", localAddOn, e)
            }
        }
    }

    private suspend fun doUninstallAddOn(localAddOn: LocalAddOn) {
        withContext(Dispatchers.IO) {
            try {
                Files.delete(localAddOn.path)
            } catch (e: IOException) {
                log.error("Failed to delete add-on: {}", localAddOn, e)
            }
        }
    }

    private suspend fun doUpdateAddOn(localAddOn: LocalAddOn, listing: AddOnListing) {
        log.info("Updating add-on '{}' @ '{}' ", listing.addOnName, localAddOn.path)
        downloadAddOn(listing, localAddOn.path)
    }

    private suspend fun downloadAddOn(listing: AddOnListing, targetPath: Path? = null) {
        if (listing.download == null) {
            throw IllegalArgumentException("Listing for add-on '${listing.id}' does not have a download URL")
        }

        val downloadType = when {
            listing.download.downloadUrl.endsWith(".zip") -> DownloadType.Archive
            listing.download.downloadUrl.endsWith(".dll") -> DownloadType.Dll
            else -> throw IllegalArgumentException("Unknown download type for URL: ${listing.download.downloadUrl}")
        }

        val targetPath = targetPath ?: let {
            val gameDirectory = configurationService.localConfiguration.firstOrNull()?.selectedGameDirectory
                ?: throw IllegalStateException("No game directory selected")

            val targetDirectory = when (listing.installMode) {
                AddOnListing.InstallMode.Arc -> gameDirectory.resolve("addons/arcdps")
                AddOnListing.InstallMode.Gw2Load -> gameDirectory.resolve("addons/${listing.addOnName}")
            }

            val targetFileName = when (downloadType) {
                DownloadType.Archive -> Url(listing.download.downloadUrl).segments.last()
                DownloadType.Dll -> when (listing.installMode) {
                    AddOnListing.InstallMode.Arc -> Url(listing.download.downloadUrl).segments.last()
                    AddOnListing.InstallMode.Gw2Load -> "${listing.id}.dll"
                }
            }

            targetDirectory.resolve(targetFileName)
        }

        val downloadTargetPath = targetPath.resolveSibling("${targetPath.fileName}.tmp")

        withContext(Dispatchers.IO) {
            Files.createDirectories(downloadTargetPath.parent)

            try {
                FileChannel.open(downloadTargetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { outputChannel ->
                    addOnRepository.download(listing.download).use { inputChannel ->
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

                                val entryPath = targetPath.parent.resolve(Path.of(entry.name).let {
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
                        Files.move(downloadTargetPath, targetPath, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            } finally {
                Files.deleteIfExists(downloadTargetPath)
            }
        }
    }

    private fun findAllDependencies(id: AddOnId, addOnListings: Map<AddOnId, AddOnListing>): Set<AddOnId> {
        val queue = ArrayDeque<AddOnId>()
        queue += id

        val dependencies = mutableSetOf<AddOnId>()

        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            val listing = addOnListings[currentId] ?: error("Could not find listing for add-on: $currentId")

            for (dependency in listing.dependencies) {
                if (dependencies.add(dependency)) {
                    queue.add(dependency)
                }
            }
        }

        return dependencies.toSet()
    }

    private fun findAllDependencies(listing: AddOnListing, addOnListings: Map<AddOnId, AddOnListing>): Set<AddOnListing> {
        val queue = ArrayDeque<AddOnListing>()
        queue += listing

        val dependencies = mutableSetOf<AddOnListing>()

        while (queue.isNotEmpty()) {
            val currentListing = queue.removeFirst()
            for (dependency in currentListing.dependencies) {
                val dependencyListing = addOnListings[dependency] ?: error("Could not find listing for add-on: $dependency")
                if (dependencies.add(dependencyListing)) {
                    queue.add(dependencyListing)
                }
            }
        }

        return dependencies.toSet()
    }

    private fun findLocalDependents(id: AddOnId, addOnListings: Map<AddOnId, AddOnListing>, localAddOns: List<LocalAddOn>): Set<LocalAddOn> {
        val localAddOnsToDependencies = localAddOns
            .associateWith { localAddOn ->
                val id = localAddOn.ref.resolveListing(addOnListings.values) ?: return@associateWith emptySet()
                findAllDependencies(id, addOnListings)
            }

        return localAddOnsToDependencies.filterValues { dependencies -> dependencies.any { dependency -> dependency.id == id } }.keys
    }

    private fun LocalAddOnReference.resolveListing(addOnListings: Iterable<AddOnListing>): AddOnListing? =
        addOnListings.find { listing -> listing isMatching this }

    private fun Action.resolveAddOnId(addOnListings: Map<AddOnId, AddOnListing>): AddOnId? =
        affectedAddOnId ?: affectedLocalAddOn?.let { ref -> addOnListings.values.find { it isMatching ref } }?.id

    private fun getSideEffects(
        action: Action,
        addOnListings: Map<AddOnId, AddOnListing>,
        localAddOns: List<LocalAddOn>,
    ): Iterable<Action> {
        val addOnId = action.resolveAddOnId(addOnListings) ?: let {
            log.warn("Failed to resolve add-on: $action")
            return emptyList()
        }

        return when (action) {
            is ActionEnableAddOn, is ActionInstallAddOn -> {
                val allDependencies = findAllDependencies(addOnId, addOnListings)

                allDependencies.mapNotNull { dependency ->
                    val localAddOn = localAddOns.find { addOnListings[dependency]?.isMatching(it) == true }

                    when {
                        localAddOn == null -> {
                            val listing = addOnListings[dependency] ?: error("Could not find listing for add-on: $dependency")
                            ActionInstallAddOn(id = listing.id)
                        }
                        !localAddOn.isEnabled -> ActionEnableAddOn(ref = localAddOn.ref)
                        else -> null
                    }
                }
            }
            is ActionDisableAddOn, is ActionUninstallAddOn -> {
                val dependents = findLocalDependents(addOnId, addOnListings, localAddOns)

                when (action) {
                    is ActionDisableAddOn -> dependents.mapNotNull { dependent ->
                        if (dependent.isEnabled) ActionDisableAddOn(ref = dependent.ref) else null
                    }
                    is ActionUninstallAddOn -> dependents.map { dependent ->
                        ActionUninstallAddOn(ref = dependent.ref)
                    }
                }
            }
            is ActionRenameAddOn, is ActionUpdateAddOn -> {
                /*
                 * Renaming or updating an add-on has no side effects on its dependencies or dependents.
                 *
                 * 1. We update the add-on in-place, so disabled add-ons remain disabled.
                 * 2. We don't have a notion of depending on a specific version of an add-on, so dependencies are
                 *    always satisfied.
                 */
                emptySet()
            }
        }
    }

    override suspend fun execute(plan: ActionPlan): OperationResult {
        val allAddOnListings = addOnListings.first()
            .associateBy(AddOnListing::id)

        val allLocalAddons = localAddOns.first()

        return jobService.runJob(
            addOnListings = plan.actions.mapNotNull { it.affectedAddOnId },
            localAddOns = plan.actions.mapNotNull { it.affectedLocalAddOn }
        ) {
            val allActionsToExecute = plan.actions + plan.effects
            val sideEffects = allActionsToExecute.flatMap { action -> getSideEffects(action, allAddOnListings, allLocalAddons) }.toSet()

            val migrations = buildMap<Migrator<*>, Iterable<Migration>> migrations@{
                val migrationContext = object : MigrationContext {

                    override val addOnListings: Iterable<AddOnListing> get() = allAddOnListings.values

                    override val localAddOns: Iterable<LocalAddOn> get() =
                        allLocalAddons.filter { localAddOn -> (allActionsToExecute + sideEffects).none { action -> action is ActionUninstallAddOn && localAddOn.ref == action.affectedLocalAddOn } }

                    override fun LocalAddOnReference.hasMigration(migrator: Migrator<*>): Boolean =
                        this@migrations[migrator]?.any { migration -> this in migration.affectedRefs } ?: false

                    override fun hasMigrator(migrator: Migrator<*>): Boolean =
                        migrator in InspectionMigrationPossible.migrators

                }

                for (migrator in InspectionMigrationPossible.migrators) {
                    val migrations = with(migrator) {
                        migrationContext.migrate()
                    }

                    put(migrator, migrations)
                }
            }
                .values
                .flatten()

            if (sideEffects.any { it !in allActionsToExecute } || (plan.stage == Stage.PROPOSED && migrations.isNotEmpty())) {
                return@runJob OperationResult.RequiresConfirmation(plan.copy(
                    effects = sideEffects.filterNot { it in plan.actions }.toSet(),
                    optionalActions = migrations.flatMap(Migration::migrate).toSet()
                ))
            }

            for (action in allActionsToExecute) {
                when (action) {
                    is ActionEnableAddOn -> {
                        val localAddOn = allLocalAddons.find { it.ref == action.ref } ?: error("Could not find local add-on: ${action.ref}")
                        doEnableAddOn(localAddOn)
                    }
                    is ActionDisableAddOn -> {
                        val localAddOn = allLocalAddons.find { it.ref == action.ref } ?: error("Could not find local add-on: ${action.ref}")
                        doDisableAddOn(localAddOn)
                    }
                    is ActionInstallAddOn -> {
                        // The error path should never be hit because we check for missing listings before executing the plan.
                        val listing = allAddOnListings[action.id] ?: error("Could not find listing for add-on: ${action.id}")
                        doInstallAddOn(listing)
                    }
                    is ActionRenameAddOn -> {
                        val localAddOn = allLocalAddons.find { it.ref == action.ref } ?: error("Could not find local add-on: ${action.ref}")
                        doRenameAddOn(localAddOn, action.newFileName)
                    }
                    is ActionUninstallAddOn -> {
                        val localAddOn = allLocalAddons.find { it.ref == action.ref } ?: error("Could not find local add-on: ${action.ref}")
                        doUninstallAddOn(localAddOn)
                    }
                    is ActionUpdateAddOn -> {
                        val localAddOn = allLocalAddons.find { it.ref == action.ref } ?: error("Could not find local add-on: ${action.ref}")
                        val listing = allAddOnListings[action.id] ?: error("Could not find listing for add-on: ${action.id}")
                        doUpdateAddOn(localAddOn, listing)
                    }
                }
            }

            loaderService.verifyLoader()

            OperationResult.Success
        }
    }

    override suspend fun disableAddOns(refs: Iterable<LocalAddOnReference>): OperationResult {
        val plan = ActionPlan(
            stage = Stage.PROPOSED,
            actions = refs.map(::ActionDisableAddOn).toSet(),
            effects = emptySet(),
            optionalActions = emptySet()
        )

        return execute(plan)
    }

    override suspend fun enableAddOns(refs: Iterable<LocalAddOnReference>): OperationResult {
        val plan = ActionPlan(
            stage = Stage.PROPOSED,
            actions = refs.map(::ActionEnableAddOn).toSet(),
            effects = emptySet(),
            optionalActions = emptySet()
        )

        return execute(plan)
    }

    override suspend fun installAddOns(ids: Iterable<AddOnId>): OperationResult {
        val plan = ActionPlan(
            stage = Stage.PROPOSED,
            actions = ids.map { id -> ActionInstallAddOn(id = id) }.toSet(),
            effects = emptySet(),
            optionalActions = emptySet()
        )

        return execute(plan)
    }

    override suspend fun uninstallAddOns(refs: Iterable<LocalAddOnReference>): OperationResult {
        val plan = ActionPlan(
            stage = Stage.PROPOSED,
            actions = refs.map(::ActionUninstallAddOn).toSet(),
            effects = emptySet(),
            optionalActions = emptySet()
        )

        return execute(plan)
    }

    override suspend fun updateAddOns(updates: Iterable<AvailableAddOnUpdate>): OperationResult {
        val plan = ActionPlan(
            stage = Stage.PROPOSED,
            actions = updates.map { update -> ActionUpdateAddOn(update.localRef, update.addOnId) }.toSet(),
            effects = emptySet(),
            optionalActions = emptySet()
        )

        return execute(plan)
    }

    override suspend fun refresh() {
        addOnRepository.invalidateCache()

        val addOnListings = addOnRepository.getAddOnListings()
        _addOnListings.emit(addOnListings)
    }

    private enum class DownloadType { Archive, Dll }

}
