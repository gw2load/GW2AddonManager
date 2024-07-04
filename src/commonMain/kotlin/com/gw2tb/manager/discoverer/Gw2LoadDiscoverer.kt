package com.gw2tb.manager.discoverer

import com.gw2tb.manager.model.LocalAddOn
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/**
 * An add-on discoverer that discovers GW2Load itself.
 *
 * @param libraryName   the (file) name of the GW2Load shared library
 */
class Gw2LoadDiscoverer(
    private val libraryName: String = "msimg32.dll",
    private val addOnName: String = "GW2Load"
) : AddOnDiscoverer {

    override fun getAddOns(gameDirectory: Path): List<LocalAddOn> {
        val libraryPath = gameDirectory.resolve(libraryName)
        val disabledLibraryPath = gameDirectory.resolve("$libraryName.disabled")

        return buildList {
            if (libraryPath.isRegularFile()) {
                add(LocalAddOn(
                    kind = LocalAddOn.Kind.GW2_LOAD_LOADER,
                    name = addOnName,
                    path = libraryPath,
                    version = "0.0.0", // TODO Read version from the library
                    isEnabled = true
                ))
            }

            if (disabledLibraryPath.isRegularFile()) {
                add(LocalAddOn(
                    kind = LocalAddOn.Kind.GW2_LOAD_LOADER,
                    name = addOnName,
                    path = disabledLibraryPath,
                    version = "0.0.0", // TODO Read version from the library
                    isEnabled = false
                ))
            }
        }
    }

}