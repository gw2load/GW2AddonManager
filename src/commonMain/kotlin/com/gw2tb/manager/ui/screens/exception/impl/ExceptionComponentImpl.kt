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
package com.gw2tb.manager.ui.screens.exception.impl

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.gw2tb.manager.exceptions.ManagerException
import com.gw2tb.manager.ui.screens.exception.ExceptionComponent
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.awt.datatransfer.StringSelection
import java.net.URI
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

class ExceptionComponentImpl(
    override val exception: ManagerException,
    mainContext: CoroutineContext,
    componentContext: ComponentContext
) : ExceptionComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope(mainContext + SupervisorJob())

    override val report = buildString {
        append(exception.cause)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun copy(clipboard: Clipboard) {
        val nativeClipEntry = StringSelection(report)

        coroutineScope.launch {
            clipboard.setClipEntry(ClipEntry(nativeClipEntry))
        }
    }

    override fun openDirectory(path: Path) {
        Desktop.getDesktop().browse(path.toUri())
    }

    override fun openLink(url: String) {
        Desktop.getDesktop().browse(URI(url))
    }

}
