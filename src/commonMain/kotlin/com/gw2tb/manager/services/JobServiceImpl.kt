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

import com.gw2tb.manager.addon_manifest.AddOnId
import com.gw2tb.manager.model.LocalAddOnReference
import io.ktor.util.collections.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

fun JobService(): JobService = JobServiceImpl()

private class JobServiceImpl : JobService {

    private val jobList = ConcurrentSet<Job>()

    private val _jobs = MutableSharedFlow<List<Job>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val jobs = _jobs

    private fun emit() {
        _jobs.tryEmit(jobList.toList())
    }

    override suspend fun <T> runJob(
        addOnListings: Iterable<AddOnId>,
        localAddOns: Iterable<LocalAddOnReference>,
        block: suspend () -> T
    ): T {
        val job = Job(
            addOnListings = addOnListings,
            localAddOns = localAddOns
        )

        jobList.add(job)
        emit()

        try {
            return block()
        } finally {
            jobList.remove(job)
            emit()
        }
    }

}
