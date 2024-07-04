package com.gw2tb.manager.discoverer

import com.gw2tb.manager.model.LocalAddOn
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createFile

/** Unit tests for [Gw2LoadDiscoverer]. */
class Gw2LoadDiscovererTest {

    @Test
    fun `Discover enabled and disabled`(@TempDir tmpDir: Path) {
        tmpDir.resolve("msimg32.dll").createFile()
        tmpDir.resolve("msimg32.dll.disabled").createFile()

        val gw2LoadDiscoverer = Gw2LoadDiscoverer()
        val discoveredAddOns = gw2LoadDiscoverer.getAddOns(tmpDir)
        assertEquals(2, discoveredAddOns.size)

        val addOn0 = discoveredAddOns[0]
        assertEquals(LocalAddOn.Kind.GW2_LOAD_LOADER, addOn0.kind)
        assertEquals("GW2Load", addOn0.name)
        assertEquals(tmpDir.resolve("msimg32.dll"), addOn0.path)
        assertEquals("0.0.0", addOn0.version)
        assertTrue(addOn0.isEnabled)

        val addOn1 = discoveredAddOns[1]
        assertEquals(LocalAddOn.Kind.GW2_LOAD_LOADER, addOn1.kind)
        assertEquals("GW2Load", addOn1.name)
        assertEquals(tmpDir.resolve("msimg32.dll.disabled"), addOn1.path)
        assertEquals("0.0.0", addOn1.version)
        assertFalse(addOn1.isEnabled)
    }

    @Test
    fun `Discover enabled only`(@TempDir tmpDir: Path) {
        tmpDir.resolve("msimg32.dll").createFile()

        val gw2LoadDiscoverer = Gw2LoadDiscoverer()
        val discoveredAddOns = gw2LoadDiscoverer.getAddOns(tmpDir)
        assertEquals(1, discoveredAddOns.size)

        val addOn0 = discoveredAddOns[0]
        assertEquals(LocalAddOn.Kind.GW2_LOAD_LOADER, addOn0.kind)
        assertEquals("GW2Load", addOn0.name)
        assertEquals(tmpDir.resolve("msimg32.dll"), addOn0.path)
        assertEquals("0.0.0", addOn0.version)
        assertTrue(addOn0.isEnabled)
    }

    @Test
    fun `Discover disabled only`(@TempDir tmpDir: Path) {
        tmpDir.resolve("msimg32.dll.disabled").createFile()

        val gw2LoadDiscoverer = Gw2LoadDiscoverer()
        val discoveredAddOns = gw2LoadDiscoverer.getAddOns(tmpDir)
        assertEquals(1, discoveredAddOns.size)

        val addOn0 = discoveredAddOns[0]
        assertEquals(LocalAddOn.Kind.GW2_LOAD_LOADER, addOn0.kind)
        assertEquals("GW2Load", addOn0.name)
        assertEquals(tmpDir.resolve("msimg32.dll.disabled"), addOn0.path)
        assertEquals("0.0.0", addOn0.version)
        assertFalse(addOn0.isEnabled)
    }

    @Test
    fun `Discover none`(@TempDir tmpDir: Path) {
        val gw2LoadDiscoverer = Gw2LoadDiscoverer()
        val discoveredAddOns = gw2LoadDiscoverer.getAddOns(tmpDir)
        assertEquals(0, discoveredAddOns.size)
    }

    @Test
    fun `Discover alternate name`(@TempDir tmpDir: Path) {
        tmpDir.resolve("gw2l.dll").createFile()
        tmpDir.resolve("gw2l.dll.disabled").createFile()

        val gw2LoadDiscoverer = Gw2LoadDiscoverer(
            libraryName = "gw2l.dll",
            addOnName = "GW2L"
        )
        val discoveredAddOns = gw2LoadDiscoverer.getAddOns(tmpDir)
        assertEquals(2, discoveredAddOns.size)
        assertEquals("GW2L", discoveredAddOns[0].name)
        assertEquals("GW2L", discoveredAddOns[1].name)
    }

}