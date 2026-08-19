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
package com.gw2tb.manager.deploy.tasks

import com.gw2tb.manager.manager_manifest.AddOnManagerManifest
import com.gw2tb.manager.manager_manifest.parseAddOnManagerManifest
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.security.MessageDigest
import java.util.HexFormat
import kotlin.io.path.*

@DisableCachingByDefault(because = "operates as side-effect in-place")
abstract class UpdateManagerManifests : DefaultTask() {

    @get:Internal
    @get:Option(option = "manifest-dir", description = "The directory containing the manifests")
    abstract val manifestDirectory: DirectoryProperty

    @get:Input
    abstract val version: ListProperty<UShort>

    @get:Input
    abstract val versionString: Property<String>

    @get:Input
    @get:Option(option = "installer-url", description = "The URL of the installer artifact.")
    abstract val installerUrl: Property<String>

    @get:Input
    @get:Option(option = "portable-url", description = "The URL of the portable artifact.")
    abstract val portableUrl: Property<String>

    @TaskAction
    protected fun run() {
        manifestDirectory.finalizeValue()
        val directory = manifestDirectory.get().asFile.toPath()
        if (!directory.isDirectory()) directory.createDirectories()

        version.finalizeValue()
        val version = version.get()

        versionString.finalizeValue()
        val versionString = versionString.get()

        installerUrl.finalizeValue()
        val installerUrl = installerUrl.get()

        portableUrl.finalizeValue()
        val portableUrl = portableUrl.get()

        val httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        val installerSha512 = httpClient.calculateSha512Checksum(installerUrl)
        val portalSha512 = httpClient.calculateSha512Checksum(portableUrl)

        updateManifestV1(directory, version, versionString, installerUrl, installerSha512, portableUrl, portalSha512)
    }

    private fun updateManifestV1(
        directory: Path,
        version: List<UShort>,
        versionString: String,
        installerUrl: String,
        installerSha512: String,
        portableUrl: String,
        portableSha512: String,
    ) {
        val manifestPath = directory.resolve("manifest.1.json")
        var manifest = if (manifestPath.isRegularFile()) {
            parseAddOnManagerManifest(manifestPath.readText()) as AddOnManagerManifest.V1
        } else {
            AddOnManagerManifest.V1(versions = emptyList())
        }

        val newVersion = AddOnManagerManifest.V1.Version(
            version = version,
            versionString = versionString,
            artifacts = listOf(
                AddOnManagerManifest.V1.Version.Artifacts(
                    type = "installer",
                    downloadUrl = installerUrl,
                    downloadSha512 = installerSha512
                ),
                AddOnManagerManifest.V1.Version.Artifacts(
                    type = "portable",
                    downloadUrl = portableUrl,
                    downloadSha512 = portableSha512
                )
            )
        )

        manifest = manifest.copy(
            versions = buildList {
                add(newVersion)
                addAll(manifest.versions)
            }
        )

        val json = Json {
            prettyPrint = true
        }

        manifestPath.writeText(json.encodeToString(manifest))
    }

    private fun HttpClient.calculateSha512Checksum(url: String): String {
        val httpRequest = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build()

        val httpResponse = send(httpRequest, HttpResponse.BodyHandlers.ofInputStream())
        if (httpResponse.statusCode() != 200) error("Unexpected HTTP status code ${httpResponse.statusCode()} for url: $url")

        val digest = MessageDigest.getInstance("SHA-512")

        httpResponse.body().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while ((input.read(buffer).also { bytesRead = it }) != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        return HexFormat.of().formatHex(digest.digest())
    }

}
