/*
 * GW2AddOnManager
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
import io.ktor.util.collections.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

fun JobService(): JobService = JobServiceImpl()

// TODO this is a very basic implementation, we should probably have a more robust one

private class JobServiceImpl : JobService {

    private val jobList = ConcurrentSet<Job>()

    private val _jobs = MutableSharedFlow<List<Job>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val jobs = _jobs

    private fun emit() {
        _jobs.tryEmit(jobList.toList())
    }

    override suspend fun <T> runJob(
        addOnListings: List<AddOnListing>,
        localAddOns: List<LocalAddOn>,
        block: suspend () -> T
    ) {
        val job = Job(
            addOnListings = addOnListings,
            localAddOns = localAddOns
        )

        jobList.add(job)
        emit()

        try {
            block()
        } finally {
            jobList.remove(job)
            emit()
        }
    }

}