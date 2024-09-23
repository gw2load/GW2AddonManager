package com.gw2tb.manager.discoverer

import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.platform.win32.getAddOnInfo
import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import java.lang.foreign.ValueLayout.*
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * An add-on discoverer that uses GW2Load to discover add-ons.
 *
 *
 * @param libraryPath   the path to the GW2Load shared library
 * @param pattern       the pattern to match add-on files against. This defaults to `.*\.dll(\.disabled)?` to match any
 *                      DLL file, including add-ons disabled by the manager.
 *
 * @throws UnsatisfiedLinkError if the GW2Load shared library cannot be loaded, or does not export the required symbols
 */
class Gw2LoadAddOnDiscoverer(
    libraryPath: Path,
    private val pattern: String = ".*\\.dll(\\.disabled)?"
) : AddOnDiscoverer, AutoCloseable {

    // TODO We should probably guard this with thread-checks and critical sections because there should only ever be one live instance of this at a time

    companion object {

        fun isValid(path: Path): Boolean =
            Arena.ofConfined().use { arena ->
                try {
                    Gw2Load(arena, path)
                    true
                } catch (_: UnsatisfiedLinkError) {
                    false
                }
            }

    }

    private val libraryArena = Arena.ofShared()
    private val gw2Load: Gw2Load

    init {
        try {
            gw2Load = Gw2Load(libraryArena, libraryPath)
        } catch (e: UnsatisfiedLinkError) {
            libraryArena.close()
            throw IllegalArgumentException("The GW2Load shared library could not be loaded.", e)
        }
    }

    override fun close() {
        libraryArena.close()
    }

    override fun getAddOns(gameDirectory: Path): List<LocalAddOn> {
        val addOnsDirectory = gameDirectory.resolve("addons").absolutePathString()

        return gw2Load.GetAddonsInDirectory(addOnsDirectory, pattern)
            .mapNotNull {
                val addOnInfo = Path.of(it.path).getAddOnInfo()
                if (addOnInfo == null) {
                    println("Failed to get add-on info for ${it.path}")
                    return@mapNotNull null
                }

                LocalAddOn(
                    kind = LocalAddOn.Kind.GW2_LOAD_ADDON,
                    name = it.name,
                    path = Path.of(it.path),
                    version = addOnInfo.version,
                    isEnabled = it.isEnabled
                )
            }
    }

}

private fun SymbolLookup.findOrThrow(symbol: String): MemorySegment {
    return find(symbol).orElseThrow { UnsatisfiedLinkError("Symbol not found: $symbol") }
}

private class Gw2Load(
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

                println("GetAddOnsInDirectory($directory, $pattern): ${Thread.currentThread().name}")

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