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
package com.gw2tb.manager.platform.win32

import com.gw2tb.manager.model.local.AddOnFileVersion
import com.gw2tb.manager.model.local.AddOnVersion
import org.slf4j.LoggerFactory
import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import java.lang.foreign.ValueLayout.*
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.absolutePathString

private val log = LoggerFactory.getLogger(Version::class.java)

@Suppress("NOTHING_TO_INLINE", "FunctionName")
private inline fun HIWORD(v: UInt): UInt = (v shr 16) and 0xFFFFu
@Suppress("NOTHING_TO_INLINE", "FunctionName")
private inline fun LOWORD(v: UInt): UInt = v and 0xFFFFu

fun Path.getAddOnInfo(): AddOnInfo? {
    fun UInt.ifZero(other: () -> UInt): UInt = if (this == 0u) other() else this

    Arena.ofConfined().use { arena ->
        val pHandle = arena.allocate(JAVA_INT)

        val size = Version.GetFileVersionInfoSizeW(absolutePathString(), pHandle)
        if (size == 0u) return null

        val handle = pHandle.get(JAVA_INT, 0).toUInt()
        val buffer = arena.allocate(size.toLong())

        if (!Version.GetFileVersionInfoW(absolutePathString(), handle, buffer)) {
            log.warn("Could not retrieve file version info for $this")
            return null
        }

        val pFileInfoSize = arena.allocate(JAVA_INT)
        val pFileInfo = arena.allocate(ADDRESS)

        if (!Version.VerQueryValueW(buffer, "\\", pFileInfo, pFileInfoSize)) {
            log.warn("Could not retrieve file version info for $this")
            return null
        }

        val fileInfoBuffer = Arena.ofAuto().allocate(Version.FixedFileInfo.LAYOUT)
        fileInfoBuffer.copyFrom(pFileInfo.get(ADDRESS, 0).reinterpret(pFileInfoSize.get(JAVA_INT, 0).toLong()))

        val fileInfo = Version.FixedFileInfo.reinterpret(fileInfoBuffer)

        val majorVersion = HIWORD(fileInfo.fileVersionMS.ifZero(fileInfo::productVersionMS)).toUShort()
        val minorVersion = LOWORD(fileInfo.fileVersionMS.ifZero(fileInfo::productVersionMS)).toUShort()
        val patchVersion = HIWORD(fileInfo.fileVersionLS.ifZero(fileInfo::productVersionLS)).toUShort()
        val fixVersion = LOWORD(fileInfo.fileVersionLS.ifZero(fileInfo::productVersionLS)).toUShort()

        val addOnVersion = AddOnFileVersion(majorVersion, minorVersion, patchVersion, fixVersion)

        val pTranslateSize = arena.allocate(JAVA_INT)
        val pTranslate = arena.allocate(ADDRESS)

        if (!Version.VerQueryValueW(buffer, "\\VarFileInfo\\Translation", pTranslate, pTranslateSize)) {
            // TODO
            return null
        }

        val langAndCodePage = Version.LangAndCodePage.reinterpret(pTranslate.get(ADDRESS, 0))
        val langSelector = "%04X%04X".format(langAndCodePage.language.toInt(), langAndCodePage.codePage.toInt())

        val ppName = arena.allocate(ADDRESS)
        val pNameSize = arena.allocate(JAVA_INT)

        val addOnName = if (!Version.VerQueryValueW(buffer, "\\StringFileInfo\\$langSelector\\ProductName", ppName, pNameSize) || pNameSize.get(JAVA_INT, 0) == 0) {
            if (!Version.VerQueryValueW(buffer, "\\StringFileInfo\\$langSelector\\FileDescription", ppName, pNameSize) || pNameSize.get(JAVA_INT, 0) == 0) {
                log.warn("Rejected potential addon '{}': could not obtain addon name info", this)
                return null;
            } else {
                ppName.get(ADDRESS, 0).reinterpret(pNameSize.get(JAVA_INT, 0).toLong() * 2).getString(0, StandardCharsets.UTF_16LE)
                    .also { log.debug("Acquired potential addon name '{}' name via FileDescription: {}", this, it) }
            }
        } else {
            ppName.get(ADDRESS, 0).reinterpret(pNameSize.get(JAVA_INT, 0).toLong() * 2).getString(0, StandardCharsets.UTF_16LE)
                .also { log.debug("Acquired potential addon name '{}' name via ProductName: {}", this, it) }
        }

        val addOnVersionString = when {
            Version.VerQueryValueW(buffer, "\\StringFileInfo\\$langSelector\\ProductVersion", ppName, pNameSize) && pNameSize.get(JAVA_INT, 0) > 0 -> {
                ppName.get(ADDRESS, 0).reinterpret(pNameSize.get(JAVA_INT, 0).toLong() * 2).getString(0, StandardCharsets.UTF_16LE)
            }
            Version.VerQueryValueW(buffer, "\\StringFileInfo\\$langSelector\\FileVersion", ppName, pNameSize) && pNameSize.get(JAVA_INT, 0) > 0 -> {
                ppName.get(ADDRESS, 0).reinterpret(pNameSize.get(JAVA_INT, 0).toLong() * 2).getString(0, StandardCharsets.UTF_16LE)
            }
            else -> {
                "$majorVersion.$minorVersion.$patchVersion.$fixVersion"
                    .also { log.debug("Derived potential addon '{}' version string from numerical version: {}", this, it) }
            }
        }

        return AddOnInfo(
            name = addOnName,
            version = AddOnVersion(fileVersion = addOnVersion, versionString = addOnVersionString)
        )
    }
}

data class AddOnInfo(
    val name: String,
    val version: AddOnVersion
)

private object Version {

    private val libraryArena = Arena.ofAuto()

    private val symbolLookup = SymbolLookup.libraryLookup("Version", libraryArena)
        .or(Linker.nativeLinker().defaultLookup())

    private fun SymbolLookup.findOrThrow(symbol: String): MemorySegment {
        return find(symbol).orElseThrow { UnsatisfiedLinkError("Symbol not found: $symbol") }
    }

    private val WORD get() = JAVA_SHORT
    private val DWORD get() = JAVA_INT

    private val GetFileVersionInfoW = let {
        val descriptor = FunctionDescriptor.of(
            JAVA_BOOLEAN,
            ADDRESS,
            DWORD,
            DWORD,
            ADDRESS
        )
        val address = symbolLookup.findOrThrow("GetFileVersionInfoW")
        Linker.nativeLinker().downcallHandle(address, descriptor)
    }

    @Suppress("FunctionName")
    fun GetFileVersionInfoW(fileName: String, handle: UInt, data: MemorySegment): Boolean {
        Arena.ofConfined().use { arena ->
            try {
                @Suppress("NAME_SHADOWING") val fileName = arena.allocateFrom(fileName, StandardCharsets.UTF_16LE)
                return (GetFileVersionInfoW.invokeExact(fileName, handle.toInt(), data.byteSize().toInt(), data) as Boolean)
            } catch (t: Throwable) {
                throw AssertionError("This should never be reached", t)
            }
        }
    }

    private val GetFileVersionInfoSizeW = let {
        val descriptor = FunctionDescriptor.of(
            DWORD,
            ADDRESS,
            ADDRESS
        )
        val address = symbolLookup.findOrThrow("GetFileVersionInfoSizeW")
        Linker.nativeLinker().downcallHandle(address, descriptor)
    }

    @Suppress("FunctionName")
    fun GetFileVersionInfoSizeW(fileName: String, handle: MemorySegment): UInt {
        Arena.ofConfined().use { arena ->
            try {
                @Suppress("NAME_SHADOWING") val fileName = arena.allocateFrom(fileName, StandardCharsets.UTF_16LE)
                return (GetFileVersionInfoSizeW.invokeExact(fileName, handle) as Int).toUInt()
            } catch (t: Throwable) {
                throw AssertionError("This should never be reached", t)
            }
        }
    }

    private val VerQueryValueW = let {
        val descriptor = FunctionDescriptor.of(
            JAVA_BOOLEAN,
            ADDRESS,
            ADDRESS,
            ADDRESS,
            ADDRESS
        )
        val address = symbolLookup.findOrThrow("VerQueryValueW")
        Linker.nativeLinker().downcallHandle(address, descriptor)
    }

    fun VerQueryValueW(block: MemorySegment, subBlock: String, buffer: MemorySegment, pLength: MemorySegment): Boolean {
        Arena.ofConfined().use { arena ->
            try {
                @Suppress("NAME_SHADOWING") val subBlock = arena.allocateFrom(subBlock, StandardCharsets.UTF_16LE)
                return (VerQueryValueW.invokeExact(block, subBlock, buffer, pLength) as Boolean)
            } catch (t: Throwable) {
                throw AssertionError("This should never be reached", t)
            }
        }
    }

    @JvmInline
    value class LangAndCodePage private constructor(private val memory: MemorySegment) {

        companion object {

            val LAYOUT: MemoryLayout = MemoryLayout.structLayout(
                WORD.withName("wLanguage"),
                WORD.withName("wCodePage")
            )

            fun reinterpret(memory: MemorySegment): LangAndCodePage =
                LangAndCodePage(memory.reinterpret(LAYOUT.byteSize()))
        }

        val language: UShort
            get() = memory.get(WORD, LAYOUT.byteOffset(groupElement("wLanguage"))).toUShort()

        val codePage: UShort
            get() = memory.get(WORD, LAYOUT.byteOffset(groupElement("wCodePage"))).toUShort()

    }

    @JvmInline
    value class VersionInfo private constructor(private val memory: MemorySegment) {

        companion object {

            val LAYOUT: MemoryLayout = MemoryLayout.structLayout(
                WORD.withName("length"),
                WORD.withName("valueLength"),
                WORD.withName("type"),
                MemoryLayout.sequenceLayout(1, WORD).withName("key"),
                WORD.withName("padding1"),
                FixedFileInfo.LAYOUT.withName("value"),
                WORD.withName("padding2"),
                WORD.withName("children")
            )

        }

        val value: FixedFileInfo
            get() = FixedFileInfo.reinterpret(memory.asSlice(LAYOUT.byteOffset(groupElement("value")), FixedFileInfo.LAYOUT))

    }

    @JvmInline
    value class FixedFileInfo private constructor(private val memory: MemorySegment) {

        companion object {

            val LAYOUT: MemoryLayout = MemoryLayout.structLayout(
                DWORD.withName("signature"),
                DWORD.withName("strucVersion"),
                DWORD.withName("fileVersionMS"),
                DWORD.withName("fileVersionLS"),
                DWORD.withName("productVersionMS"),
                DWORD.withName("productVersionLS"),
                DWORD.withName("fileFlagsMask"),
                DWORD.withName("fileFlags"),
                DWORD.withName("fileOS"),
                DWORD.withName("fileType"),
                DWORD.withName("fileSubtype"),
                DWORD.withName("fileDateMS"),
                DWORD.withName("fileDateLS")
            )

            fun reinterpret(memory: MemorySegment): FixedFileInfo =
                FixedFileInfo(memory.reinterpret(LAYOUT.byteSize()))

        }

        val signature: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("signature"))).toUInt()

        val strucVersion: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("strucVersion"))).toUInt()

        val fileVersionMS: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("fileVersionMS"))).toUInt()

        val fileVersionLS: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("fileVersionLS"))).toUInt()

        val productVersionMS: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("productVersionMS"))).toUInt()

        val productVersionLS: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("productVersionLS"))).toUInt()

        val fileFlagsMask: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("fileFlagsMask"))).toUInt()

        val fileFlags: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("fileFlags"))).toUInt()

        val fileOS: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("fileOS"))).toUInt()

        val fileType: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("fileType"))).toUInt()

        val fileSubtype: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("fileSubtype"))).toUInt()

        val fileDateMS: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("fileDateMS"))).toUInt()

        val fileDateLS: UInt
            get() = memory.get(DWORD, LAYOUT.byteOffset(groupElement("fileDateLS"))).toUInt()

    }

}