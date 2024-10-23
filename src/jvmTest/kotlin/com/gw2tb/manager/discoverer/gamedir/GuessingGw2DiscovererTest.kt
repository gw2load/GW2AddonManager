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
package com.gw2tb.manager.discoverer.gamedir

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GuessingGw2DiscovererTest {

    @Test
    fun `testFindGameDirectory - Success`(@TempDir tmpDir: Path) {
        val testExecutablePath = tmpDir.resolve("test.txt") // Not actually an executable

        testExecutablePath.writeText("Hello World")
        assertEquals(
            expected = testExecutablePath.parent,
            actual = GuessingGw2Discoverer(testExecutablePath).findGameDirectory()
        )
    }

    @Test
    fun `testFindGameDirectory - File does not exist`(@TempDir tmpDir: Path) {
        val testExecutablePath = tmpDir.resolve("test.txt")
        assertNull(GuessingGw2Discoverer(testExecutablePath).findGameDirectory())
    }

    @Test
    fun `testFindGameDirectory - Directory is treated as null`(@TempDir tmpDir: Path) {
        val testExecutablePath = tmpDir.resolve("test.txt")

        testExecutablePath.createDirectory()
        assertNull(GuessingGw2Discoverer(testExecutablePath).findGameDirectory())
    }

}