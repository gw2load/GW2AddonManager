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
package com.gw2tb.manager.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchEvent
import java.nio.file.WatchService
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

fun Path.watchDirectory(
    events: Array<WatchEvent.Kind<*>> = arrayOf(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW),
    vararg modifiers: WatchEvent.Modifier,
    filter: (Path, WatchEvent<*>?) -> Boolean = { _, _ -> true }
): Flow<WatchEvent<*>?> {
    val watchService = fileSystem.newWatchService()

    return watchDirectory(
        watchService = watchService,
        events = events,
        modifiers = modifiers,
        filter = filter
    )
        .onCompletion { watchService.close() }
}

private fun Path.watchDirectory(
    watchService: WatchService,
    events: Array<WatchEvent.Kind<*>> = arrayOf(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW),
    vararg modifiers: WatchEvent.Modifier,
    filter: (Path, WatchEvent<*>?) -> Boolean = { _, _ -> true }
): Flow<WatchEvent<*>?> {
    require(isDirectory()) { "Path is not a directory: ${this.absolutePathString()}" }

    val registeredKey = register(watchService, events, *modifiers)

    return callbackFlow {
        send(InitialWatchEvent)

        val job = launch(Dispatchers.IO) {
            while (true) {
                val key = watchService.take()
                if (key != registeredKey) continue

                for (event in key.pollEvents()) {
                    if (!filter(event.context() as Path, event)) {
                        continue
                    }

                    send(event)
                }

                if (!key.reset()) {
                    break
                }
            }
        }

        awaitClose {
            launch {
                job.cancelAndJoin()
                registeredKey.cancel()
            }
        }
    }
}

fun Path.watchFile(
    events: Array<WatchEvent.Kind<*>> = arrayOf(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW)
): Flow<WatchEvent<*>?> {
    require(isRegularFile()) { "Path is not a file: ${this.absolutePathString()}" }

    return parent.watchDirectory(
        events = events,
        filter = { path, _ -> parent.resolve(path) == this }
    )
}

private object InitialWatchEventKind : WatchEvent.Kind<Any> {
    override fun name(): String = "INITIAL"
    override fun type(): Class<Any> = Any::class.java
}

private object InitialWatchEvent : WatchEvent<Any> {
    override fun context(): Any? = null
    override fun count(): Int = 1
    override fun kind(): WatchEvent.Kind<Any> = InitialWatchEventKind
}
