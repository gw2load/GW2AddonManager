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
package com.gw2tb.manager.discoverer.loader

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BOOLEAN
import java.lang.foreign.ValueLayout.JAVA_INT
import java.nio.file.Path

class Gw2Load(
    arena: Arena,
    libraryPath: Path
) {

    private val symbolLookup = SymbolLookup.libraryLookup(libraryPath, arena)
        .or(Linker.nativeLinker().defaultLookup())

    @Suppress("PrivatePropertyName")
    private val GetAddonsInDirectory = let {
        val descriptor = FunctionDescriptor.of(
            ADDRESS,
            ADDRESS,
            ADDRESS,
            ADDRESS
        )
        val address = symbolLookup.findOrThrow("GW2Load_GetAddonsInDirectory")
        Linker.nativeLinker().downcallHandle(address, descriptor)
    }

    @Suppress("FunctionName")
    fun GetAddonsInDirectory(directory: String, pattern: String): List<EnumeratedAddOn> {
        Arena.ofConfined().use { arena ->
            try {
                @Suppress("NAME_SHADOWING") val directory = arena.allocateFrom(directory)
                @Suppress("NAME_SHADOWING") val pattern = arena.allocateFrom(pattern)

                val pCount = arena.allocate(JAVA_INT, 1)
                var res = GetAddonsInDirectory.invokeExact(directory, pCount, pattern) as MemorySegment
                val count = pCount.get(JAVA_INT, 0)

                res = res.reinterpret(count * EnumeratedAddOn.LAYOUT.byteSize())

                return List(size = count) { index ->
                    EnumeratedAddOn.reinterpret(res.asSlice(index * EnumeratedAddOn.LAYOUT.byteSize(), EnumeratedAddOn.LAYOUT))
                }
            } catch (t: Throwable) {
                throw AssertionError("This should never be reached", t)
            }
        }
    }

    @JvmInline
    value class EnumeratedAddOn private constructor(private val memory: MemorySegment) {

        companion object {

            val LAYOUT: MemoryLayout = MemoryLayout.structLayout(
                ADDRESS.withName("path"),
                ADDRESS.withName("name"),
                JAVA_BOOLEAN.withName("isEnabled"),
                MemoryLayout.paddingLayout(7)
            )

            fun reinterpret(memory: MemorySegment): EnumeratedAddOn =
                EnumeratedAddOn(memory.reinterpret(LAYOUT.byteSize()))

        }

        val path: String
            get() = memory.get(ADDRESS, LAYOUT.byteOffset(groupElement("path"))).reinterpret(Long.MAX_VALUE).getString(0)

        val name: String
            get() = memory.get(ADDRESS, LAYOUT.byteOffset(groupElement("name"))).reinterpret(Long.MAX_VALUE).getString(0)

        val isEnabled: Boolean
            get() = memory.get(JAVA_BOOLEAN, LAYOUT.byteOffset(groupElement("isEnabled")))

    }

}
