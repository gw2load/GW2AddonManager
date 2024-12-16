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

import com.gw2tb.manager.AppInfo
import com.gw2tb.manager.model.update.ManagerVersion
import com.gw2tb.manager.repository.UpdateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.absolutePathString

fun UpdateService(
    updateRepository: UpdateRepository,
    configurationService: ConfigurationService,
    jobService: JobService,
    appInfo: AppInfo
): UpdateService = UpdateServiceImpl(
    updateRepository = updateRepository,
    configurationService = configurationService,
    jobService = jobService,
    appInfo = appInfo
)

private class UpdateServiceImpl(
    private val updateRepository: UpdateRepository,
    private val configurationService: ConfigurationService,
    private val jobService: JobService,
    private val appInfo: AppInfo
) : UpdateService {

    private companion object {
        val log: Logger = LoggerFactory.getLogger(UpdateServiceImpl::class.java)
    }

    override val availableUpdate: Flow<ManagerVersion> = flow {
        // TODO: Enhance appInfo and perform a version check
        emit(updateRepository.getManagerVersion())
    }

    override suspend fun install(update: ManagerVersion) {
        log.info("Installing update: ${update.downloadUrl}")

        jobService.runJob { // TODO: Block all other jobs
            log.debug("Downloading update")
            val downloadTargetPath = configurationService.tempDirectoryLayout.installerPath
            Files.createDirectories(downloadTargetPath.parent)
            Files.deleteIfExists(downloadTargetPath)

            FileChannel.open(downloadTargetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { outputChannel ->
                updateRepository.download(update).use { inputChannel ->
                    outputChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE)
                }
            }

            log.debug("Installing update via msiexec")
            val process = ProcessBuilder()
                .command("msiexec", "/package", downloadTargetPath.absolutePathString())
                .start()

            val exitCode = process.waitFor()
            log.error("msiexec exited unexpectedly with code $exitCode")
        }
    }

}