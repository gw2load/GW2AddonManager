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

import com.gw2tb.manager.internal.BuildConfig
import com.gw2tb.manager.model.ManagerVersion
import com.gw2tb.manager.repository.ManagerVersionRepository
import io.github.z4kn4fein.semver.Version
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart

fun UpdateService(
    versionRepository: ManagerVersionRepository
): UpdateService = UpdateServiceImpl(
    versionRepository = versionRepository
)

class UpdateServiceImpl(
    private val versionRepository: ManagerVersionRepository
) : UpdateService {

    private val _availableUpdate = MutableSharedFlow<ManagerVersion?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val availableUpdate: Flow<ManagerVersion?> =
        _availableUpdate
            .onStart { emit(findAvailableUpdate()) }

    override suspend fun refresh() {
        versionRepository.invalidateCache()
        _availableUpdate.emit(findAvailableUpdate())
    }

    private suspend fun findAvailableUpdate(): ManagerVersion? {
        val versions = versionRepository.getVersions()
        val currentVersion = Version.parse(BuildConfig.BUILD_VERSION)

        return versions.firstOrNull {
            val version = Version.parse(it.versionString)
            version > currentVersion
        }
    }

}
