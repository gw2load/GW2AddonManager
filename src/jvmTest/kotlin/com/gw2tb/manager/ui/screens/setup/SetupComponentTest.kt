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
package com.gw2tb.manager.ui.screens.setup

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.gw2tb.manager.model.LocalConfiguration
import com.gw2tb.manager.services.ConfigurationService
import com.gw2tb.manager.ui.screens.setup.impl.SetupComponentImpl
import org.junit.jupiter.api.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import java.nio.file.Path
import kotlin.test.assertEquals

class SetupComponentTest {

    @Test
    suspend fun testConfirmSetup() {
        val configurationService: ConfigurationService = mock()
        val output: (SetupComponent.Output) -> Unit = mock()

        val component = SetupComponentImpl(
            configurationService = configurationService,
            output = output,
            componentContext = DefaultComponentContext(LifecycleRegistry())
        )

        component.selectGameDirectory(Path.of("S:\\Program Files\\Guild Wars 2"))

        component.selectedGameDirectory.test {
            assertEquals(
                Path.of("S:\\Program Files\\Guild Wars 2"),
                awaitItem()
            )
        }

        val inOrder = inOrder(configurationService, output)
        component.confirmSetup()

        inOrder.verify(configurationService).save(LocalConfiguration(selectedGameDirectory = Path.of("S:\\Program Files\\Guild Wars 2"), gameDirectories = listOf(Path.of("S:\\Program Files\\Guild Wars 2"))))
        inOrder.verify(output)(SetupComponent.Output.ConfirmSetup)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    suspend fun testSelectGameDirectory() {
        val configurationService: ConfigurationService = mock()
        val output: (SetupComponent.Output) -> Unit = mock()

        val component = SetupComponentImpl(
            configurationService = configurationService,
            output = output,
            componentContext = DefaultComponentContext(LifecycleRegistry())
        )

        val inOrder = inOrder(configurationService, output)
        component.selectGameDirectory(Path.of("S:\\Program Files\\Guild Wars 2"))

        component.selectedGameDirectory.test {
            assertEquals(
                Path.of("S:\\Program Files\\Guild Wars 2"),
                awaitItem()
            )
        }

        inOrder.verifyNoMoreInteractions()
    }

}
