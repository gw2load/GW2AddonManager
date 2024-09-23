package com.gw2tb.manager

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.app_name
import com.gw2tb.manager.gw2addonmanager.generated.resources.icon
import com.gw2tb.manager.repository.AddOnRepositoryImpl
import com.gw2tb.manager.services.*
import com.gw2tb.manager.ui.AddOnManager
import com.gw2tb.manager.ui.composables.LocalApplicationInfo
import com.gw2tb.manager.ui.impl.AddOnManagerComponentImpl
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("Main")

fun main() {
    log.info("Starting the GW2 Add-On Manager")

    val appInfo = readApplicationInfo()
    log.info("Application version: ${appInfo.version}")

    runApplication(appInfo)

    log.debug("Exiting normally")
}

private fun runApplication(appInfo: AppInfo) {
    val configurationService = ConfigurationService()
    val jobService = JobService()

    /* Let's play nice and use a single HTTP client with a proper user agent for all our requests. */
    val httpClient = HttpClient {
        install(Logging) {
            level = LogLevel.ALL
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        install(UserAgent) {
            agent = "GW2AddonManager/${appInfo.version}"
        }
    }

    val addOnService = AddOnService(
        addOnRepository = AddOnRepositoryImpl(
            httpClient = httpClient
        ),
        configurationService = configurationService,
        jobService = jobService
    )

    val notificationService = NotificationService(
        addOnService = addOnService,
        configurationService = configurationService
    )

    val lifecycle = LifecycleRegistry()

    application {
        /*
         * We use a remember block to ensure that the component is not recreated on every recomposition. This is not
         * strictly necessary here but neat for correctness in case the application block is ever recomposed.
         *
         * Ideally, we would lift this out of the application block but at that point the AWT event loop is not yet
         * initialized, causing Decompose's thread checks to fail.
         */
        val component = remember {
            AddOnManagerComponentImpl(
                addOnService = addOnService,
                configurationService = configurationService,
                jobService = jobService,
                notificationService = notificationService,
                componentContext = DefaultComponentContext(lifecycle)
            )
        }

        CompositionLocalProvider(LocalApplicationInfo provides appInfo) {
            Window(
                onCloseRequest = ::exitApplication,
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
                    width = 1130.dp,
                    height = 600.dp
                ),
                title = stringResource(Res.string.app_name),
                icon = painterResource(Res.drawable.icon),
                undecorated = true,
                transparent = true,
                resizable = false
            ) {
                /*
                 * Due to its Android origins, Compose enables a minimum size for interactive (material) components by
                 * default. Since we are using some material components under the hood for now, we need to disable this
                 * because it's mostly useless padding in a desktop environment.
                 */
                CompositionLocalProvider(@OptIn(ExperimentalMaterialApi::class) LocalMinimumInteractiveComponentEnforcement provides false) {
                    AddOnManager(
                        component = component,
                        minimizeWindow = { window.isMinimized = true },
                        exitApplication = ::exitApplication
                    )
                }
            }
        }
    }
}