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
package com.gw2tb.manager.repository

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import com.gw2tb.manager.AppInfo
import com.gw2tb.manager.exceptions.ManagerManifestException
import com.gw2tb.manager.internal.BuildConfig
import com.gw2tb.manager.manager_manifest.AddOnManagerManifest
import com.gw2tb.manager.manager_manifest.parseAddOnManagerManifest
import com.gw2tb.manager.model.ManagerVersion
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.http.buildUrl
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.io.path.isRegularFile

/** A repository for manager versions. */
interface ManagerVersionRepository : AutoCloseable {

    /**
     * Returns a list of available manager versions.
     *
     * @return  a list of all manager versions
     */
    suspend fun getVersions(): FetchResult<List<ManagerVersion>>

    /** Invalidates the repository's cache. */
    fun invalidateCache()

}

class ManagerVersionRepositoryImpl(
    private val httpClient: HttpClient,
    appInfo: AppInfo
) : ManagerVersionRepository {

    private companion object {

        private const val CACHE_KEY = "manager-manifest"

        private const val MANIFEST_VERSION = 1

        private val log = LogManager.getLogger()

    }

    private val desiredArtifactType =
        if (appInfo.applicationDir.resolve(".installed").isRegularFile()) {
            "installer"
        } else {
            "portable"
        }


    private val cacheScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val managerManifestCache: AsyncLoadingCache<String, FetchResult<AddOnManagerManifest>> = Caffeine.newBuilder()
        .refreshAfterWrite(Duration.ofMinutes(10))
        .expireAfterWrite(Duration.ofHours(6))
        .buildAsync(object : CacheLoader<String, FetchResult<AddOnManagerManifest>> {

            override fun load(key: String): FetchResult<AddOnManagerManifest> {
                error("Not implemented")
            }

            override fun asyncLoad(key: String, executor: Executor): CompletableFuture<out FetchResult<AddOnManagerManifest>> {
                return cacheScope.future { fetchManifest().map(::parseAddOnManagerManifest) }
            }

            override fun asyncReload(key: String, oldValue: FetchResult<AddOnManagerManifest>, executor: Executor): CompletableFuture<out FetchResult<AddOnManagerManifest>> {
                return cacheScope.future {
                    when (val fetchResult = asyncLoad(key, executor).await()) {
                        is FetchResultWithException -> oldValue.withException(fetchResult.cause)
                        else -> fetchResult
                    }
                }
            }

        })

    override fun close() {
        cacheScope.cancel()
    }

    override suspend fun getVersions(): FetchResult<List<ManagerVersion>> {
        return managerManifestCache.get(CACHE_KEY).await().map { manifest ->
            when (manifest) {
                is AddOnManagerManifest.V1 -> manifest.versions.mapNotNull(::mapToDomainObject)
            }
        }
    }

    override fun invalidateCache() {
        log.debug("Invalidating cache")
        managerManifestCache.synchronous().invalidate(CACHE_KEY)
    }

    private suspend fun fetchManifest(): FetchResult<String> {
        val httpResponse = try {
            withContext(Dispatchers.IO) {
                httpClient.get(buildUrl {
                    takeFrom(BuildConfig.MANAGER_MANIFEST_BASE_URL)
                    appendPathSegments("manifest.${MANIFEST_VERSION}.json")
                })
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch manifest", e)
            return FetchResult.failed(ManagerManifestException.FetchException(e))
        }

        if (!httpResponse.status.isSuccess()) {
            val e = Throwable("Unexpected HTTP status code: ${httpResponse.status}")
            return FetchResult.failed(ManagerManifestException.FetchException(httpResponse.status, e))
        }

        return FetchResult.Success(httpResponse.bodyAsText())
    }

    private fun mapToDomainObject(version: AddOnManagerManifest.V1.Version): ManagerVersion? {
        return ManagerVersion(
            versionString = version.versionString,
            downloadUrl = version.artifacts.find { it.type == desiredArtifactType }?.downloadUrl ?: return null
        )
    }

}
