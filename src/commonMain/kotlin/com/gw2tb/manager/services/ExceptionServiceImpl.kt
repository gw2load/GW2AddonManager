/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2026 Leon Linhart
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

import com.gw2tb.manager.exceptions.AddOnManifestException
import com.gw2tb.manager.exceptions.ManagerException
import com.gw2tb.manager.exceptions.UnexpectedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

fun ExceptionService(
    addOnService: AddOnService,
    loaderService: LoaderService,
    updateService: UpdateService
): ExceptionService = ExceptionServiceImpl(
    addOnService = addOnService,
    loaderService = loaderService,
    updateService = updateService
)

class ExceptionServiceImpl(
    val addOnService: AddOnService,
    val loaderService: LoaderService,
    val updateService: UpdateService
) : ExceptionService {

    private val uncaughtExceptions = MutableStateFlow<List<ManagerException>>(emptyList())

    override val exceptions: Flow<List<ManagerException>> =
        combine(
            addOnService.addOnListingManifestException,
            loaderService.loaderListingManifestException,
            updateService.managerManifestException,
            uncaughtExceptions
        ) { addOnListingManifestException, loaderManifestException, managerManifestException, uncaughtExceptions ->
            buildList {
                if (addOnListingManifestException is AddOnManifestException && loaderManifestException is AddOnManifestException) {
                    // If both exceptions are issues with the manifest, we only need to propagate a single one
                    add(addOnListingManifestException)
                } else {
                    addOnListingManifestException?.let(::add)
                    loaderManifestException?.let(::add)
                }

                managerManifestException?.let(::add)

                // TODO Consider reworking this eventually to group uncaught exceptions together
                addAll(uncaughtExceptions)
            }
        }

    override fun handleException(e: Throwable) {
        uncaughtExceptions.update { it + UnexpectedException(e) }
    }

}
