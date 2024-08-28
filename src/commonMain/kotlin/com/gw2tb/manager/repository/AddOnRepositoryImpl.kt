package com.gw2tb.manager.repository

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.gw2tb.manager.model.AddOnListing
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.time.Duration

/**
 * An implementation of [AddOnRepository] that fetches add-on information from a
 *
 */
class AddOnRepositoryImpl(
    private val httpClient: HttpClient,
    private val cache: AsyncCache<String, String> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .buildAsync()
) : AddOnRepository {

    private companion object {
        private val log = LogManager.getLogger()
    }

    override suspend fun getAddOnListings(): List<AddOnListing> = withContext(Dispatchers.IO) {
        val manifest = cache.get("addon-repo/manifest") { _, _ ->
            future {
                val httpResponse = httpClient.get("https://knoxfighter.github.io/addon-repo/manifest.json")
                httpResponse.bodyAsText()
            }
        }.await()

        parsePluginManifest(manifest)
            .map {
                AddOnListing(
                    id = it.`package`.id,
                    addOnName = it.`package`.name,
                    addOnSummary = it.`package`.tooltip,
                    addOnDescription = it.`package`.description,

                    homepageUrl = it.`package`.website,
                    issueTrackerUrl = it.`package`.issueTracker,
                    vcsUrl = it.`package`.vcs,

                    vendorName = it.`package`.developer,
                    vendorUrl = it.`package`.website,

                    downloadUrl = it.release!!.downloadUrl,
                    versionName = it.release.versionString,
                    addOnNames = it.addonNames ?: emptyList(),

                    installMode = when (it.installation.mode) {
                        AddonRepositoryManifest.AddOnEntry.Installation.Mode.ARC -> AddOnListing.InstallMode.Arc
                        AddonRepositoryManifest.AddOnEntry.Installation.Mode.GW2LOAD -> AddOnListing.InstallMode.Gw2Load
                    },
                    dependencies = it.`package`.dependencies ?: emptyList()
                )
            }
            .sortedBy(AddOnListing::addOnName)
    }

    override suspend fun download(listing: AddOnListing): ReadableByteChannel {
        val httpResponse = httpClient.get(Url(listing.downloadUrl))
        return Channels.newChannel(httpResponse.bodyAsChannel().toInputStream())
    }

    override fun invalidateCache() {
        log.info("Invalidating add-on repository cache")
        cache.synchronous().invalidateAll()
    }

}