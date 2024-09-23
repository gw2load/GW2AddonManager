package com.gw2tb.manager.util

import com.sun.nio.file.ExtendedWatchEventModifier
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchEvent

fun Path.watch(): Flow<WatchEvent<*>> {
    val watchService = fileSystem.newWatchService()
    register(watchService, arrayOf(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW), ExtendedWatchEventModifier.FILE_TREE)

    return callbackFlow {
        val job = launch {
            while (true) {
                val key = watchService.take()

                for (event in key.pollEvents()) {
                    trySend(event)
                }

                if (!key.reset()) {
                    break
                }
            }
        }

        awaitClose {
            launch {
                job.cancelAndJoin()
                watchService.close()
            }
        }
    }
}