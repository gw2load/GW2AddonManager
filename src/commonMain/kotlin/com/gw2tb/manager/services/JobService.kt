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

import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.local.LocalAddOn
import kotlinx.coroutines.flow.Flow

interface JobService {

    val jobs: Flow<List<Job>>

    suspend fun <T> runJob(
        addOnListings: List<AddOnListing> = emptyList(),
        localAddOns: List<LocalAddOn> = emptyList(),
        block: suspend () -> T
    )

}

class Job(
    val addOnListings: List<AddOnListing>,
    val localAddOns: List<LocalAddOn>
)