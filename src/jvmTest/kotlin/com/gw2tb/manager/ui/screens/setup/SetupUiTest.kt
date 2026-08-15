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
package com.gw2tb.manager.ui.screens.setup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.*
import java.nio.file.Path

@OptIn(ExperimentalTestApi::class)
class SetupUiTest {

    @Test
    fun `testConfirmSetupButton - Game Directory Selected`(@TempDir gameDirectory: Path) = runComposeUiTest {
        val component = spy(DummySetupComponent(initiallySelectedGameDirectory = gameDirectory))

        setContent {
            SetupScreen(component)
        }

        onNodeWithTag("ConfirmSetupButton")
            .assertIsEnabled()
            .performClick()

        verify(component).confirmSetup()
    }

    @Test
    fun `testConfirmSetupButton - No Directory Selected`() = runComposeUiTest {
        val component = DummySetupComponent(initiallySelectedGameDirectory = null)

        setContent {
            SetupScreen(component)
        }

        onNodeWithTag("ConfirmSetupButton")
            .assertIsNotEnabled()
    }

}
