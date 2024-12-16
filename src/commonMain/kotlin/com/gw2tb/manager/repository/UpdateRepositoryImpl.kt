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
package com.gw2tb.manager.repository

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.gw2tb.manager.model.update.ManagerVersion
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import org.slf4j.LoggerFactory
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.time.Duration

class UpdateRepositoryImpl(
    private val httpClient: HttpClient,
    private val cache: AsyncCache<String, String> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .buildAsync()
) : UpdateRepository {

    private companion object {
        private val log = LoggerFactory.getLogger(UpdateRepositoryImpl::class.java)
    }

    override suspend fun getManagerVersion(): ManagerVersion {
        // TODO Implement version fetching and parsing

        return ManagerVersion(
            downloadUrl = ""
        )
    }

    override suspend fun download(update: ManagerVersion): ReadableByteChannel {
        val httpResponse = httpClient.get(update.downloadUrl)
        return Channels.newChannel(httpResponse.bodyAsChannel().toInputStream())
    }

    override fun invalidateCache() {
        log.info("Invalidating update repository cache")
        cache.synchronous().invalidateAll()
    }

}