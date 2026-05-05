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

import com.gw2tb.manager.exceptions.ManagerException
import com.gw2tb.manager.internal.BuildConfig
import com.gw2tb.manager.model.ManagerVersion
import com.gw2tb.manager.repository.FetchResult
import com.gw2tb.manager.repository.FetchResultWithException
import com.gw2tb.manager.repository.FetchResultWithValue
import com.gw2tb.manager.repository.ManagerVersionRepository
import com.gw2tb.manager.repository.map
import com.osmerion.kotlin.semver.Version
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            .onStart { refresh() }

    private val _managerManifestException = MutableStateFlow<ManagerException?>(null)
    override val managerManifestException: Flow<ManagerException?> = _managerManifestException.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    override suspend fun refresh() {
        versionRepository.invalidateCache()

        val result = findAvailableUpdate()
        _managerManifestException.emit((result as? FetchResultWithException)?.cause)
        _availableUpdate.emit((result as? FetchResultWithValue<ManagerVersion>)?.value)
    }

    private suspend fun findAvailableUpdate(): FetchResult<ManagerVersion?> {
        return versionRepository.getVersions().map { versions ->
            val currentVersion = Version.parse(BuildConfig.BUILD_VERSION)

            versions.firstOrNull {
                val version = Version.parse(it.versionString)
                version > currentVersion
            }
        }
    }

}
