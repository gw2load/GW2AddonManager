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
import com.gw2tb.manager.addon_manifest.AddOnManifestV1
import com.gw2tb.manager.addon_manifest.parseAddOnManifest
import com.gw2tb.manager.internal.BuildConfig
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.catalog.Download
import com.gw2tb.manager.model.catalog.LoaderListing
import com.gw2tb.manager.model.local.AddOnVersion
import com.gw2tb.manager.util.fileinfo.FileVersion
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import java.lang.AutoCloseable
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.time.Duration


/** A repository for add-ons. */
interface AddOnRepository : AutoCloseable {

    /**
     * Returns a list of add-on listings available from the repository.
     *
     * @return  the available add-ons
     */
    suspend fun getAddOnListings(): List<AddOnListing>

    suspend fun getLoader(): LoaderListing?

    /**
     * Opens a download channel for the given listing.
     *
     * @param download  the listing to download
     *
     * @return  the channel to read from
     */
    suspend fun download(download: Download): ReadableByteChannel

    /** Invalidates the add-on listing cache. */
    fun invalidateCache()

}

class AddOnRepositoryImpl(
    private val httpClient: HttpClient,
    private val cache: AsyncCache<String, String> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .buildAsync()
) : AddOnRepository {

    private companion object {

        private const val CACHE_KEY = "addon-manifest"

        private val log = LogManager.getLogger()

    }

    private val cacheScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun close() {
        cacheScope.cancel()
    }

    override suspend fun getAddOnListings(): List<AddOnListing> {
        val manifestString = try {
            cache.get(CACHE_KEY) { _, _ ->
                cacheScope.future {
                    fetchManifest()
                        .also(::parseAddOnManifest)
                }
            }.await()
        } catch (e: Exception) {
            log.warn("Could not load manifest", e)
            return emptyList()
        }

        return when (val manifest = parseAddOnManifest(manifestString)) {
            is AddOnManifestV1 -> manifest.addons
                .map(::mapToDomainObject)
                .sortedBy(AddOnListing::addOnName)
        }
    }

    override suspend fun getLoader(): LoaderListing? {
        val manifestString = try {
            cache.get(CACHE_KEY) { _, _ ->
                cacheScope.future {
                    fetchManifest()
                        .also(::parseAddOnManifest)
                }
            }.await()
        } catch (e: Exception) {
            log.warn("Could not load manifest", e)
            return null
        }

        return when (val manifest = parseAddOnManifest(manifestString)) {
            is AddOnManifestV1 -> mapToDomainObject(manifest.loader)
        }
    }

    override suspend fun download(download: Download): ReadableByteChannel {
        val httpResponse = httpClient.get(Url(download.downloadUrl))
        return Channels.newChannel(httpResponse.bodyAsChannel().toInputStream())
    }

    override fun invalidateCache() {
        log.info("Invalidating add-on repository cache")
        cache.synchronous().invalidateAll()
    }

    private suspend fun fetchManifest(): String {
        val httpResponse = try {
            withContext(Dispatchers.IO) {
                httpClient.get(urlString = BuildConfig.ADDON_MANIFEST_URL)
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch manifest", e)
            throw e
        }

        if (!httpResponse.status.isSuccess()) {
            throw IllegalStateException("Failed to fetch manifest: ${httpResponse.status}")
        }

        return httpResponse.bodyAsText()
    }

    private fun mapToDomainObject(entry: AddOnManifestV1.AddOnEntry): AddOnListing {
        return AddOnListing(
            id = entry.`package`.id,
            addOnName = entry.`package`.name,
            addOnSummary = entry.`package`.tooltip,
            addOnDescription = entry.`package`.description,

            homepageUrl = entry.`package`.website,
            issueTrackerUrl = entry.`package`.issueTracker,
            vcsUrl = entry.`package`.vcs,

            vendorName = entry.`package`.developer,
            vendorUrl = entry.`package`.website,

            download = entry.release?.let { release ->
                Download(
                    downloadUrl = release.downloadUrl,
                    AddOnVersion(
                        fileVersion = release.version.let { (a, b, c, d) -> FileVersion(a.toUShort(), b.toUShort(), c.toUShort(), d.toUShort()) },
                        versionString = release.versionString
                    )
                )
            },
            addOnNames = entry.addonNames ?: emptyList(),

            installMode = when (entry.installation.mode) {
                AddOnManifestV1.AddOnEntry.Installation.Mode.ARC -> AddOnListing.InstallMode.Arc
                AddOnManifestV1.AddOnEntry.Installation.Mode.GW2LOAD -> AddOnListing.InstallMode.Gw2Load
            },
            dependencies = entry.`package`.dependencies ?: emptyList()
        )
    }

    private fun mapToDomainObject(entry: AddOnManifestV1.Loader): LoaderListing {
        return LoaderListing(
            release = entry.release?.let { release ->
                Download(
                    downloadUrl = release.downloadUrl,
                    AddOnVersion(
                        fileVersion = release.version.let { (a, b, c, d) -> FileVersion(a.toUShort(), b.toUShort(), c.toUShort(), d.toUShort()) },
                        versionString = release.versionString
                    )
                )
            },
            prerelease = entry.prerelease?.let { release ->
                Download(
                    downloadUrl = release.downloadUrl,
                    AddOnVersion(
                        fileVersion = release.version.let { (a, b, c, d) -> FileVersion(a.toUShort(), b.toUShort(), c.toUShort(), d.toUShort()) },
                        versionString = release.versionString
                    )
                )
            }
        )
    }

}
