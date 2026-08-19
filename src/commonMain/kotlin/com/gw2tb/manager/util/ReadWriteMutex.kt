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
/* INTENTIONALLY NO LICENSE HEADER, SEE BELOW */
package com.gw2tb.manager.util

import com.gw2tb.manager.util.MutexMode.*
import com.gw2tb.manager.util.MutexState.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Taken from https://gist.github.com/bobvawter/4ff642d5996dfccb228425909f303306
// See also: https://github.com/Kotlin/kotlinx.coroutines/issues/94

/**
 * Copyright 2017 ModelBox Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * The read or write mode of the [ReadWriteMutex].
 */
enum class MutexMode {
    READ,
    WRITE
}

/**
 * The lock state of the [ReadWriteMutex].
 */
enum class MutexState {
    /**
     * The mutex has been locked.
     */
    LOCKED,
    /**
     * The mutex has been unlocked.
     */
    UNLOCKED
}

data class MutexInfo(val mode: MutexMode, val state: MutexState)

/**
 * A variation on [Mutex] that supports read and write operations.
 *
 * A "real" version may be in the works:
 * https://github.com/Kotlin/kotlinx.coroutines/issues/94
 */
interface ReadWriteMutex {
    /**
     * Execute a read operation.  Readers do not block one another, but will be blocked
     * by a writer.
     */
    suspend fun <T> withReadLock(block: suspend () -> T): T

    /**
     * Execute a write operation.  Writers block all other activity in the mutex until
     * they have finished.
     */
    suspend fun <T> withWriteLock(fn: suspend () -> T): T
}

/**
 * Construct a new [ReadWriteMutex].  The [block] can be used to provide additional
 * configuration options to the mutex.
 */
fun ReadWriteMutex(block: ReadWriteMutexBuilder.() -> Unit = {}): ReadWriteMutex {
    return ReadWriteMutexBuilder().apply(block).build()
}

class ReadWriteMutexBuilder internal constructor() {
    private var onStateChange: (suspend (MutexInfo) -> Unit)? = null

    /**
     * Provide a function to invoke as the mutex transitions into new states.  This can be
     * used to coordinate the internal operation with externally-visible effects, such as
     * acquiring filesystem locks.
     */
    fun onStateChange(block: suspend (MutexInfo) -> Unit) {
        onStateChange = block
    }

    internal fun build(): ReadWriteMutex = ReadWriteMutexImpl(onStateChange)
}

/**
 * Implementation of [ReadWriteMutex].
 */
private class ReadWriteMutexImpl constructor(
    private val onStateChange: (suspend (MutexInfo) -> Unit)?) : ReadWriteMutex {
    /**
     * A mutex to guard the creation of new readers.
     */
    private val allowNewReads = Mutex()
    /**
     * A mutex to guard the creation of new writers.
     */
    private val allowNewWrites = Mutex()
    private var readers = 0
    /**
     * Controls access to [readers].
     */
    private val stateLock = Mutex()

    override suspend fun <T> withReadLock(block: suspend () -> T): T {
        return try {
            // Ensure new readers are allowed
            allowNewReads.withLock {
                stateLock.withLock {
                    // Increment the reader count.
                    if (readers++ == 0) {
                        // If we're the first reader, ensure that writes are locked out
                        allowNewWrites.lock(this)
                        // Invoke user callback
                        onStateChange?.invoke(MutexInfo(READ, LOCKED))
                    }
                }
            }
            // Execute the user function.
            block()
        } finally {
            // We don't want to use allowNewReads here, because a writer can acquire that lock
            // while waiting fol allowNewWrites to be unlocked.  Instead, we'll just treat clean-up
            // like we're draining all outstanding readers to admit the writer.
            stateLock.withLock {
                // Decrement the reader count, and unlock if we were the last reader
                if (--readers == 0) {
                    try {
                        // Invoke user callback in opposite order from above
                        onStateChange?.invoke(MutexInfo(READ, UNLOCKED))
                    } finally {
                        // If a writer is pending, this will unlock it
                        allowNewWrites.unlock(this)
                    }
                }
            }
        }
    }

    override suspend fun <T> withWriteLock(fn: suspend () -> T): T {
        // Prevent readers from starting any new action
        return allowNewReads.withLock {
            // Wait for all outstanding readers to drain.
            allowNewWrites.withLock {
                try {
                    onStateChange?.invoke(MutexInfo(WRITE, LOCKED))
                    fn()
                } finally {
                    onStateChange?.invoke(MutexInfo(WRITE, UNLOCKED))
                }
            }
        }
    }
}
