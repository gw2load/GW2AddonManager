package com.gw2tb.manager.ui.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.gw2tb.manager.model.LocalConfiguration
import com.gw2tb.manager.model.Notification
import com.gw2tb.manager.services.*
import com.gw2tb.manager.ui.AddOnManagerComponent
import com.gw2tb.manager.ui.AddOnManagerComponent.Child
import com.gw2tb.manager.ui.screens.explore.impl.ExploreComponentImpl
import com.gw2tb.manager.ui.screens.manage.impl.ManageComponentImpl
import com.gw2tb.manager.ui.screens.settings.impl.SettingsComponentImpl
import com.gw2tb.manager.ui.screens.setup.SetupComponent
import com.gw2tb.manager.ui.screens.setup.impl.SetupComponentImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.awt.Desktop
import java.net.URI

class AddOnManagerComponentImpl(
    private val addOnService: AddOnService,
    configurationService: ConfigurationService,
    jobService: JobService,
    notificationService: NotificationService,
    componentContext: ComponentContext
) : AddOnManagerComponent, ComponentContext by componentContext {

    // TODO Properly scope coroutines
    private val mainContext = Dispatchers.Default
    private val coroutineScope = coroutineScope(mainContext + SupervisorJob())

    private val localConfiguration: StateFlow<LocalConfiguration?> =
        configurationService.localConfiguration.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = null)

    override val jobs: StateFlow<List<Job>> =
        jobService.jobs.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    override val notifications: StateFlow<List<Notification>> =
        notificationService.notifications.stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = emptyList())

    private val navigation = StackNavigation<Config>()

    override val page: Value<ChildStack<*, Child>> = childStack(
        source = navigation,
        serializer = null,
        initialConfiguration = localConfiguration.value.let { localConfiguration ->
            /* If there is no local configuration yet, or if it's invalid, we'll start in the setup screen. */
            if (localConfiguration == null || !configurationService.isValid(localConfiguration)) Config.Setup else Config.ExploreAddOns()
        },
        handleBackButton = false,
        childFactory = { config, _ ->
            when (config) {
                is Config.ExploreAddOns -> Child.ExploreAddOns(ExploreComponentImpl(
                    addOnService = addOnService,
                    configurationService = configurationService,
                    jobService = jobService,
                    selectedAddOnId = config.selectedAddOnId,
                    mainContext = mainContext,
                    componentContext = componentContext
                ))
                is Config.ManageAddOns -> Child.InstalledAddOns(ManageComponentImpl(
                    addOnService = addOnService,
                    configurationService = configurationService,
                    jobService = jobService,
                    selectedAddOnName = config.selectedAddOnName,
                    mainContext = mainContext,
                    componentContext = componentContext
                ))
                is Config.Settings -> Child.Settings(SettingsComponentImpl(
                    configurationService = configurationService,
                    mainContext = mainContext,
                    componentContext = componentContext
                ))
                is Config.Setup -> Child.Setup(SetupComponentImpl(
                    configurationService = configurationService,
                    output = { output ->
                        when (output) {
                            is SetupComponent.Output.ConfirmSetup -> navigation.replaceCurrent(Config.ExploreAddOns())
                        }
                    },
                    componentContext = componentContext
                ))
            }
        }
    )

    @Serializable
    private sealed class Config {

        @Serializable
        data class ExploreAddOns(val selectedAddOnId: String? = null) : Config()

        @Serializable
        data class ManageAddOns(val selectedAddOnName: String?) : Config()

        @Serializable
        data object Settings : Config()

        @Serializable
        data object Setup : Config()

    }

    override fun navigateToExploreAddOns() {
        val activeChild = page.active.instance
        val selectedAddOnId = let {
            if (activeChild is Child.InstalledAddOns) {
                val selectedAddOn = activeChild.component.selectedAddOn.value ?: return@let null
                val addOnListings = activeChild.component.addOnListings.value

                addOnListings.firstOrNull { selectedAddOn.name in it.addOnNames }?.id
            } else {
                null
            }
        }

        navigation.replaceCurrent(Config.ExploreAddOns(selectedAddOnId = selectedAddOnId))
    }

    override fun navigateToInstalledAddOns() {
        val activeChild = page.active.instance
        val selectedAddOnId = let {
            if (activeChild is Child.ExploreAddOns) {
                val selectedAddOn = activeChild.component.selectedAddOn.value ?: return@let null
                val localAddOns = activeChild.component.localAddOns.value

                localAddOns.firstOrNull { it.name in selectedAddOn.addOnNames }?.name
            } else {
                null
            }
        }

        navigation.replaceCurrent(Config.ManageAddOns(selectedAddOnName = selectedAddOnId))
    }

    override fun navigateToSettings() {
        navigation.replaceCurrent(Config.Settings)
    }

    override fun onNotificationClick(notification: Notification) {
        coroutineScope.launch {
            notification.quickFix?.invoke()
        }
    }

    override fun openLink(url: String) {
        Desktop.getDesktop().browse(URI(url))
    }

    override fun play() {
        val localConfiguration = localConfiguration.value ?: error("Local configuration is not available")
        val gameDirectory = localConfiguration.selectedGameDirectory ?: error("No game directory selected")

        Desktop.getDesktop().open(gameDirectory.resolve("Gw2-64.exe").toFile())
    }

}