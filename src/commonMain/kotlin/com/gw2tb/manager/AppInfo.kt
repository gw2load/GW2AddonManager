package com.gw2tb.manager

import org.slf4j.LoggerFactory
import java.net.URL
import java.util.jar.JarFile
import java.util.jar.Manifest

private val log = LoggerFactory.getLogger("AppInfo")

data class AppInfo(val version: String)

private object Res

internal fun readApplicationInfo(): AppInfo {
    val cls = Res::class.java
    val classLoader = cls.classLoader

    val resUrl = classLoader.getResource("com/gw2tb/manager/Res.class")
    if (resUrl != null) {
        try {
            if (resUrl.protocol == "jar" || resUrl.protocol == "file") {
                val manifestUrl = cls.getResource("/${JarFile.MANIFEST_NAME}")
                val appInfo = readApplicationInfo(manifestUrl!!)
                if (appInfo != null) return appInfo
            }
        } catch (e: Exception) {
            log.warn("Unexpected error while reading application info", e)
        }
    }

    return AppInfo(
        version = "dev"
    )
}

private fun readApplicationInfo(url: URL): AppInfo? {
    return try {
        url.openStream().use { inputStream ->
            val manifest = Manifest(inputStream)
            val attributes = manifest.mainAttributes

            if ("com.gw2tb.manager" != attributes.getValue("Implementation-Title")) {
                log.debug("Unexpected implementation title in manifest: ${attributes.getValue("Implementation-Title")}")
                return null
            }

            val version = attributes.getValue("Implementation-Version")
            if (version == null) {
                log.error("No implementation version in manifest")
                return null
            }

            AppInfo(
                version = version
            )
        }
    } catch (e: Exception) {
        log.warn("Unexpected error while reading application info", e)
        null
    }
}