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

import com.gw2tb.manager.addon_manifest.AddOnManifestV1
import com.gw2tb.manager.addon_manifest.parseAddOnManifest
import com.gw2tb.manager.util.fileinfo.AddOnFileInfo
import com.gw2tb.manager.util.fileinfo.FileVersion
import com.gw2tb.manager.util.fileinfo.readAddOnFileInfo
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.io.path.isRegularFile

@DisableCachingByDefault(because = "operates as side-effect in-place")
abstract class DownloadGw2Load : DefaultTask() {

    @get:Input
    abstract val manifestUrl: Property<String>

    @get:OutputFile
    abstract val destination: RegularFileProperty

    @TaskAction
    protected fun run() {
        manifestUrl.finalizeValue()
        destination.finalizeValue()

        val httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

        val manifestRequest = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(manifestUrl.get()))
            .build()

        val manifestResponse = httpClient.send(manifestRequest, HttpResponse.BodyHandlers.ofString())
        if (manifestResponse.statusCode() != 200) error("Unexpected status code while fetching manifest: ${manifestResponse.statusCode()}")

        val currentLoaderPath = destination.get().asFile.toPath()
        val currentLoaderAddOnFileInfo = if (currentLoaderPath.isRegularFile()) currentLoaderPath.readAddOnFileInfo() else null

        val manifest = parseAddOnManifest(manifestResponse.body())
        if (manifest !is AddOnManifestV1) error("Unexpected AddOnManifest version: ${manifest::class.simpleName}")

        val loaderRelease = manifest.loader.release

        if (currentLoaderAddOnFileInfo == null || loaderRelease isMoreRecentThan currentLoaderAddOnFileInfo) {
            val loaderRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(loaderRelease.downloadUrl))
                .build()

            val loaderResponse = httpClient.send(loaderRequest, HttpResponse.BodyHandlers.ofFile(currentLoaderPath))
            if (loaderResponse.statusCode() != 200) error("Unexpected status code while downloading loader: ${loaderResponse.statusCode()}")
        }
    }

    private infix fun AddOnManifestV1.Release.isMoreRecentThan(addOnFileInfo: AddOnFileInfo): Boolean {
        require(version.size == 4) { "version must have four segments" }

        val releaseVersion = FileVersion(version[0].toUShort(), version[1].toUShort(), version[2].toUShort(), version[3].toUShort())
        return releaseVersion > addOnFileInfo.version
    }

}
