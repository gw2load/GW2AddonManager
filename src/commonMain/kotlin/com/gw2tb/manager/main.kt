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
package com.gw2tb.manager

import androidx.compose.foundation.LocalIndication
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.app_name
import com.gw2tb.manager.gw2addonmanager.generated.resources.icon
import com.gw2tb.manager.internal.BuildConfig
import com.gw2tb.manager.repository.AddOnRepositoryImpl
import com.gw2tb.manager.repository.ManagerVersionRepositoryImpl
import com.gw2tb.manager.services.*
import com.gw2tb.manager.ui.AddOnManager
import com.gw2tb.manager.ui.composables.LocalAppLocaleIso
import com.gw2tb.manager.ui.composables.LocalApplicationInfo
import com.gw2tb.manager.ui.impl.RootComponentImpl
import com.gw2tb.manager.ui.theme.glimmer
import com.gw2tb.manager.util.use
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.logging.log4j.core.config.Configurator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.absolutePathString

fun main() {
    val applicationDir = System.getProperty("manager.dir")?.let(Path::of)

    // 1. Resolve the local configuration path (for PC-specific information and logs)
    val appdataPath = try {
        System.getenv("APPDATA")?.let(Path::of)!!
    } catch (e: InvalidPathException) {
        throw IllegalStateException("Invalid APPDATA path", e)
    }

    val localAppDataDirectory = appdataPath.resolve("GW2AddOnManager")
    if (!Files.isDirectory(localAppDataDirectory)) {
        Files.createDirectories(localAppDataDirectory)
    }

    // 2. Prepare logging system
    System.setProperty("logsDirectory", localAppDataDirectory.resolve("logs").absolutePathString())
    Configurator.initialize(null, "log4j2.xml")

    // 3. Launch application
    val appInfo = AppInfo(version = BuildConfig.BUILD_VERSION, applicationDir = applicationDir)
    runApplication(localAppDataDirectory, appInfo)
}

private fun runApplication(
    localAppDataDirectory: Path,
    appInfo: AppInfo
) {
    val mainContext = Dispatchers.Default

    val configurationService = ConfigurationService(
        localAppDataDirectory = localAppDataDirectory,
        mainContext = mainContext
    )

    val jobService = JobService()

    /* Let's play nice and use a single HTTP client with a proper user agent for all our requests. */
    val httpClient = HttpClient {
        install(Logging) {
            level = LogLevel.HEADERS
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        install(UserAgent) {
            agent = "GW2AddOnManager/${appInfo.version}"
        }
    }

    try {
        use(
            AddOnRepositoryImpl(httpClient = httpClient),
            ManagerVersionRepositoryImpl(httpClient = httpClient, appInfo = appInfo)
        ) { listingRepository, managerVersionRepository ->
            val loaderService = LoaderService(
                addOnRepository = listingRepository,
                configurationService = configurationService,
                appInfo = appInfo,
                mainContext = mainContext
            )

            val addOnService = AddOnService(
                addOnRepository = listingRepository,
                configurationService = configurationService,
                jobService = jobService,
                loaderService = loaderService,
                mainContext = mainContext
            )

            val inspectionService = InspectionService(
                addOnService = addOnService,
                mainContext = mainContext
            )

            val updateService = UpdateService(
                versionRepository = managerVersionRepository
            )

            val notificationService = NotificationService(
                inspectionService = inspectionService,
                updateService = updateService
            )

            val lifecycle = LifecycleRegistry()

            application {
                val coroutineScope = rememberCoroutineScope()

                /*
                 * We use a remember block to ensure that the component is not recreated on every recomposition. This is not
                 * strictly necessary here but neat for correctness in case the application block is ever recomposed.
                 *
                 * Ideally, we would lift this out of the application block but at that point the AWT event loop is not yet
                 * initialized, causing Decompose's thread checks to fail.
                 */
                val component = remember {
                    RootComponentImpl(
                        addOnService = addOnService,
                        configurationService = configurationService,
                        inspectionService = inspectionService,
                        jobService = jobService,
                        notificationService = notificationService,
                        componentContext = DefaultComponentContext(lifecycle)
                    )
                }

                CompositionLocalProvider(LocalApplicationInfo provides appInfo) {
                    Window(
                        onCloseRequest = ::exitApplication,
                        onKeyEvent = { event ->
                            when {
                                event.key == Key.F5 -> {
                                    coroutineScope.launch {
                                        addOnService.refresh()
                                        updateService.refresh()
                                    }

                                    true
                                }
                                else -> false
                            }
                        },
                        state = rememberWindowState(
                            placement = WindowPlacement.Floating,
                            position = WindowPosition.Aligned(Alignment.Center),
                            /*
                             * We always set a fixed size for the window to ensure that the UI is displayed correctly on top of the
                             * background image. For now, this is hardcoded to the size of the background image. There are some more
                             * paddings related to background image sprinkled throughout the root layout anyway, so it does not
                             * really make sense to load this dynamically. We might however, seek to consolidate these values into a
                             * single configuration.
                             */
                            width = 1155.dp,
                            height = 629.dp
                        ),
                        title = stringResource(Res.string.app_name),
                        icon = painterResource(Res.drawable.icon),
                        undecorated = true,
                        transparent = true,
                        resizable = false
                    ) {
                        var locale by remember { mutableStateOf<Locale?>(null) }

                        /*
                         * Due to its Android origins, Compose enables a minimum size for interactive (material) components by
                         * default. Since we are using some material components under the hood for now, we need to disable this
                         * because it's mostly useless padding in a desktop environment.
                         */
                        CompositionLocalProvider(
                            LocalIndication provides glimmer(),
                            @OptIn(ExperimentalMaterialApi::class) LocalMinimumInteractiveComponentEnforcement provides false,
                            LocalAppLocaleIso provides locale
                        ) {
                            AddOnManager(
                                component = component,
                                selectLocale = { locale = it },
                                minimizeWindow = { window.isMinimized = true },
                                exitApplication = ::exitApplication
                            )
                        }
                    }
                }
            }
        }
     } finally {
        httpClient.close()
    }
}
