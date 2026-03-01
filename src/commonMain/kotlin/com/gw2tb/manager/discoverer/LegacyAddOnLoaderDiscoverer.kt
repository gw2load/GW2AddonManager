/*
 * Guild Wars 2 Add-on Manager
 * Copyright (C) 2024-2025 Leon Linhart
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
package com.gw2tb.manager.discoverer

import com.gw2tb.manager.model.local.AddOnVersion
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.util.contentChecksum
import com.gw2tb.manager.util.fileinfo.FileVersion
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.fileSize

/**
 * A discoverer for the old addonloader.
 *
 * This discoverer compares the file size and content checksum of all unclaimed DLLs in the directory tree against a
 * list of known addonloader files.
 */
class LegacyAddOnLoaderDiscoverer(
    private val hashes: List<FileHash> = KNOWN_HASHES
) : AddOnDiscoverer {

    private companion object {

        private val log = LogManager.getLogger(LegacyAddOnLoaderDiscoverer::class)

        private val KNOWN_HASHES = listOf(
            /*
             * - 1.1_r35 bin64/cef/dxgi.dll
             * - 1.1_r35 dxgi.dll
             * - 1.1_r34 bin64/cef/dxgi.dll
             * - 1.1_r33 bin64/cef/dxgi.dll
             */
            FileHash(10_752, "a78bcb29bd9b94af3bb0695957f4127966c2bfbd15854c92ba4ff9069b427271dce16313e3ee61c97daf9d9889427ecf1894aebd90e62c9525c088ee25523ca4"),
            /*
             * - 1.1_r35 d3d11.dll
             * - 1.1_r34 d3d11.dll
             * - 1.1_r33 d3d11.dll
             */
            FileHash(10_752, "2c3098dc8cc9df1c4ae722de90fe1473bc7e8e07539255b90105e5a756ec20f35fc0a763994635486c6681e1994ed3b6a97f22a5ad8a5bcb730bc48ed3f6c88e"),
            /*
             * - 1.1_r35 addonLoader.dll
             * - 1.1_r34 addonLoader.dll
             * - 1.1_r33 addonLoader.dll
             */
            FileHash(37_376, "d135675ac1ec0ed2bac4b50b3c2ac84720d59318daa880f707a3718e4ba515c328ded09a004fd2e3657542e05b0977f3d2c87d3d152dab87f150c46e54dae93a"),
            /*
             * - 1.1_r27 addonLoader.dll
             */
            FileHash(37_376, "2049f75f928f4da17c029ba904d69124246bb52cc1160db7c3d801afb024c138a73edd62f8a1482ed0094e912667adf040daf7b8a1386a0c1b815bd0e09ef14e"),
            /*
             * - 1.1_r27 d3d11.dll
             */
            FileHash(10_752, "0706fb1b144020212c26e45f99f9374616c6595f8187fa57fd94636210a6738c891163e6195cebb191d4b6da8690be1270a2fc116cb9a1b33e5883e6f7a0dee7"),
            /*
             * - 1.1_r27 dxgi.dll
             */
            FileHash(10_752, "8f5c01a5ca709725c1477eee2d64633230ebdc55a1fe831bf5aaa374f8c9e012dd3453dbae1b7ae47ac5564ecaf403a9fcc322b2086e867994f62e6c6600a41e"),
            /*
             * - 1.1_r33 bin64/d3d9.dll
             */
            FileHash(10_752 ,"0c86f74c260fad76d2fcfbb43ef5cfbd20e66898fcd2bdb19a7e3cbb37e3848941296ad156353bb5fab2664bf891358b3ef06e91268e1ea46c531d2ee4d5a071")
        )

        private val KNOWN_SIZES = KNOWN_HASHES.map(FileHash::size).toSet()

    }

    override fun getAddOns(gameDirectory: Path, discoveredAddOns: List<LocalAddOn>): List<LocalAddOn> {
        return buildList {
            Files.walkFileTree(gameDirectory, object : SimpleFileVisitor<Path>() {

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (attrs.isRegularFile && attrs.size() in KNOWN_SIZES) add(file)
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                    log.warn("Skipping over subtree due to I/O exception: ${file.relativize(gameDirectory)}", exc)
                    return FileVisitResult.SKIP_SUBTREE
                }

            })
        }
            .filter(isDistinctFrom(discoveredAddOns))
            .mapNotNull(::discover)
            .toList()
    }

    private fun discover(path: Path): LocalAddOn? {
        if (!path.toString().endsWith(".dll") && !path.toString().endsWith(".dll.disabled")) return null

        val candidateHashes = hashes.filter { it.size == path.fileSize() }

        if (candidateHashes.isEmpty()) return null

        val pathChecksum = path.contentChecksum()
        val matchedFileHash = candidateHashes.find { fileHash -> fileHash.hash == pathChecksum }

        if (matchedFileHash == null) {
            log.debug("Skipping file '{}' without matching checksum", path)
            return null
        }

        return LocalAddOn(
            kind = LocalAddOn.Kind.ADDONLOADER,
            name = "addonloader",
            path = path,
            version = AddOnVersion(FileVersion(0u, 0u, 0u, 0u))
        )
    }

    data class FileHash(val size: Long, val hash: String)

}
